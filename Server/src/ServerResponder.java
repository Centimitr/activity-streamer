import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

@SuppressWarnings({"WeakerAccess", "Convert2MethodRef"})
abstract class ServerResponder extends UnicastRemoteObject implements IRemoteNode {
    static final Logger log = LogManager.getLogger();
    static final Gson g = new Gson();

    final String id = UUID.randomUUID().toString();
    final ConnectivityManager cm = new ConnectivityManager();
    final NodesManager nm = new NodesManager();
    final RegisterManager rm = new RegisterManager();
    final SessionManager sm = new SessionManager();
    final Lock recoverLock = new ReentrantLock();
    final MessageCache messageCache = new MessageCache();
    final MessageCounter messageCounter = new MessageCounter();

    ServerResponder() throws RemoteException {
        nm.local().bindConnectivitySet(cm.clients());
        RouterManager routers = cm.routerManager();
        BiConsumer<MessageContext, String> commonErrorHandler = (context, error) -> {
            String info;
            switch (error) {
                case "Parse Error":
                    info = "Json Parse Error while parsing message.";
                    break;
                case "Syntax Error":
                    info = "Message Syntax Error.";
                    break;
                default:
                    info = "Command not support.";
                    break;
            }
            MsgInvalidMessage res = new MsgInvalidMessage(info);
            context.write(res);
            context.close();
        };

        routers.temp()
                .handle(MessageCommands.REGISTER, context -> {
                    MsgRegister m = context.read(MsgRegister.class);
//                    log.info("Register: Start:" + m.username + " " + m.secret);
                    Runnable handleRegisteredRequest = () -> {
                        String info = m.username + " is already registered with the system.";
                        MsgRegisterFailed res = new MsgRegisterFailed(info);
                        context.write(res);
                        context.close();
                    };
                    boolean anonymous = m.username.equals("anonymous");
                    boolean registered = rm.has(m.username);
                    if (registered || anonymous) {
//                        log.info("Register: Local already registered: " + m.username + " " + m.secret);
                        handleRegisteredRequest.run();
                        return;
                    }
                    Util.async(() -> nm.register(id, m.username, m.secret));
                    rm.put(id, m.username, m.secret);
                    String info = "register success for " + m.username;
                    MsgRegisterSuccess res = new MsgRegisterSuccess(info);
                    context.write(res);
                    cm.temp().transfer(context.connectivity, cm.clients());
                    log.info("Register: Success:" + m.username + " " + m.secret);
                })
                .handleError((context, error) -> {
                    log.info("temp error: ", error);
                    String command = context.command;
                    switch (command) {
                        case "ACTIVITY_BROADCAST":
                        case "SERVER_ANNOUNCE":
                        case "LOCK_REQUEST":
                        case "LOCK_ALLOWED":
                        case "LOCK_DENIED":
                            String info = "Message received from unauthenticated server.";
                            MsgInvalidMessage res = new MsgInvalidMessage(info);
                            context.write(res);
                            context.close();
                            break;
                        default:
                            commonErrorHandler.accept(context, error);
                    }
                });
        routers.client()
                .handle(MessageCommands.LOGOUT, context -> {
                    sm.markAsOffline(context.get("username"));
                    context.close();
                })
                .handle(MessageCommands.ACTIVITY_MESSAGE, context -> {
                    sm.markAsOnline(context.get("username"), context.connectivity);
                    MsgActivityMessage m = context.read(MsgActivityMessage.class);
                    boolean anonymous = m.username.equals("anonymous");
                    boolean match = m.username.equals(context.get("username")) && m.secret.equals(context.get("secret"));
                    boolean loggedIn = context.get("username") != null;
                    if (!(anonymous || loggedIn && match)) {
                        String info = "username is not anonymous or no user logged in.";
                        if (!match) {
                            info = "the supplied secret is incorrect: " + m.secret;
                        }
                        MessageInfo res = new MsgAuthenticationFail(info);
                        context.write(res);
                        context.close();
                        return;
                    }
                    m.activity.put("authenticated_user", context.get("username"));
                    MsgActivityBroadcast broadcast = new MsgActivityBroadcast(m.activity);
                    nm.sendMessages(context.get("username"), messageCounter.getIndex(context.get("username")), g.toJson(broadcast));
                })
                .handle(MessageCommands.REGISTER, context -> {
                    MsgInvalidMessage res = new MsgInvalidMessage("User has already logged in.");
                    context.write(res);
                    context.close();
                })
                .handleError((context, error) -> {
                    sm.markAsOffline(context.get("username"));
                    log.info("client error: ", error);
                    commonErrorHandler.accept(context, error);
                });

        // group routing
        routers.possibleClient()
                .handle(MessageCommands.LOGIN, context -> {
                    sm.markAsOnline(context.get("username"), context.connectivity);
                    MsgLogin m = context.read(MsgLogin.class);
                    boolean anonymous = m.username.equals("anonymous");
                    boolean match = rm.match(m.username, m.secret);
                    if (!match && !anonymous) {
                        context.write(new MsgLoginFailed("attempt to login with wrong secret"));
                        context.close();
                        return;
                    }
                    context.write(new MsgLoginSuccess("logged in as user " + m.username));
                    context.set("username", m.username);
                    context.set("secret", m.secret);
                    cm.possibleClients().transfer(context.connectivity, cm.clients());
                    RemoteNode freeNode = nm.getFreeNode();
                    if (freeNode != null) {
                        context.write(new MsgRedirect(freeNode.clientHostname, freeNode.clientPort));
                        sm.markAsOffline(context.get("username"));
                        context.close();
                        return;
                    }
                    Util.async(() -> {
                        ArrayList<String> cachedMessages = messageCache.pop(context.get("username"));
                        for (String message : cachedMessages) {
                            context.connectivity.sendln(message);
                        }
                    });
                });
    }

    @Override
    public String declare(String secret, String remoteHostname, int remotePort, String clientHost, int clientPort, boolean needRecovery) throws RemoteException {
        if (!secret.equals(Settings.getSecret())) {
            return null;
        }
        log.info("Connected: " + remoteHostname + ":" + remotePort);
        String rtn = "{}";
        if (needRecovery) {
            recoverLock.lock();
            RecoveryData data = new RecoveryData(rm, nm);
            recoverLock.unlock();
            rtn = data.toJson();
        }
        nm.add(remoteHostname, remotePort, clientHost, clientPort);
        return rtn;
    }

    @Override
    public ArrayList<String> getUserList() throws RemoteException {
        return sm.getUserList();
    }

    @Override
    public ArrayList<String> sendMessage(String sender, int index, ArrayList<String> receivers, String msg, boolean retry) throws RemoteException {
        // todo: feedback
        ArrayList<String> failedUsers = new ArrayList<>();
        for (Map.Entry<String, Connectivity> entry : sm.getConnectivities().entrySet()) {
            String username = entry.getKey();
            Connectivity conn = entry.getValue();
            if (receivers.contains(username)) {
                if (retry) {
                    Util.retry(() -> conn.sendln(msg), Env.RETRY_INTERVAL, Env.SESSION_TIMEOUT);
                } else {
                    boolean ok = conn.sendln(msg);
                    if (!ok) {
                        failedUsers.add(username);
                    }
                }
            } else {
                messageCache.put(sender, index, msg);
            }
        }
        return failedUsers;
    }

    @Override
    public void register(String id, String username, String secret) throws RemoteException {
        rm.put(id, username, secret);
    }

    @Override
    public int getLoad() throws RemoteException {
        return nm.local().getLoad();
    }

}
