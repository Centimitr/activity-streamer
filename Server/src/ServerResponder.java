import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;
import java.util.function.BiConsumer;

@SuppressWarnings({"WeakerAccess", "Convert2MethodRef"})
abstract class ServerResponder extends UnicastRemoteObject implements IRemoteNode {
    static final Logger log = LogManager.getLogger();
    static final Gson g = new Gson();

    String id = UUID.randomUUID().toString();
    ConnectivityManager cm = new ConnectivityManager();
    NodesManager nm = new NodesManager();
    RegisterManager rm = new RegisterManager();
    Servers servers = new Servers(nm);
    Users users = new Users();
    Lock recoverLock = new Lock();

    ServerResponder() throws RemoteException {
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
                    boolean registered = users.has(m.username);
                    if (registered || anonymous) {
//                        log.info("Register: Local already registered: " + m.username + " " + m.secret);
                        handleRegisteredRequest.run();
                        return;
                    }

                    // ask other servers' options
                    MsgLockRequest req = new MsgLockRequest(m.username, m.secret);
                    // todo: server broadcase
//                    cm.servers().broadcast(req);
                    // todo: new wait for remotes, 0
                    boolean available = rm.wait(m.username, m.secret, 0);
                    if (!available) {
//                        log.info("Register: Remote already registered: " + m.username + " " + m.secret);
                        handleRegisteredRequest.run();
                        return;
                    }
                    users.add(m.username, m.secret);

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
                    context.close();
                })
                .handle(MessageCommands.ACTIVITY_MESSAGE, context -> {
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
                    try {
//                        JsonObject activity = (new JsonParser()).parse(m.activity).getAsJsonObject();
//                        activity.addProperty("authenticated_user", context.get("username"));
                        m.activity.put("authenticated_user", context.get("username"));
                        MsgActivityBroadcast broadcast = new MsgActivityBroadcast(m.activity);
//                        cm.servers().broadcast(broadcast);
                        // todo: server broadcast
                        cm.clients().broadcast(broadcast);
                    } catch (JsonSyntaxException e) {
                        MsgInvalidMessage res = new MsgInvalidMessage("activity object json syntax error");
                        context.write(res);
                        context.close();
                    }
                })
                .handle(MessageCommands.REGISTER, context -> {
                    MsgInvalidMessage res = new MsgInvalidMessage("User has already logged in.");
                    context.write(res);
                    context.close();
                })
                .handleError((context, error) -> {
                    log.info("client error: ", error);
                    commonErrorHandler.accept(context, error);
                });

        // group routing
        routers.possibleClient()
                .handle(MessageCommands.LOGIN, context -> {
                    MsgLogin m = context.read(MsgLogin.class);
                    boolean anonymous = m.username.equals("anonymous");
                    boolean match = users.match(m.username, m.secret);
                    if (!match && !anonymous) {
                        context.write(new MsgLoginFailed("attempt to login with wrong secret"));
                        context.close();
                        return;
                    }
                    context.write(new MsgLoginSuccess("logged in as user " + m.username));
                    context.set("username", m.username);
                    context.set("secret", m.secret);
                    cm.possibleClients().transfer(context.connectivity, cm.clients());
                    // todo: load balance: redirect
                    int currentLoad = cm.clients().size();
                    ServerRecord availableServer = servers.balancer().getAvailableServer(currentLoad);
                    boolean needRedirect = availableServer != null;
                    if (needRedirect) {
                        context.write(new MsgRedirect(availableServer.hostname, availableServer.port));
                        context.close();
                    }
                });
    }

    IRemoteNode connectNode(String hostname, int port) {
        try {
            Registry remoteRegistry = LocateRegistry.getRegistry(hostname, port);
            IRemoteNode node = (IRemoteNode) remoteRegistry.lookup("Node");
            boolean ok = node.declare(Settings.getSecret(), id, Settings.getRemoteHostname(), Settings.getRemotePort(), true);
            if (ok) {
                return node;
            }
        } catch (RemoteException | NotBoundException e) {
            // todo: maybe remote fails
            log.error("parent node:", e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean declare(String secret, String id, String remoteHostname, int remotePort, boolean needRecovery) throws RemoteException {
        if (!secret.equals(Settings.getSecret())) {
            return false;
        }
        IRemoteNode node = connectNode(remoteHostname, remotePort);
        if (node == null) {
            return false;
        }
        servers.records().put(id, remoteHostname, remotePort, 0);
        if (needRecovery) {
            // todo: add messages recovery
            node.recover(servers.snapshot(), users.snapshot());
        }
        return true;
    }

    @Override
    public void recover(String serversSnapshot, String usersSnapshot) throws RemoteException {
        servers.recover(serversSnapshot);
        users.recover(usersSnapshot);
        servers.records().forEach((id, record) -> {
            IRemoteNode node = connectNode(record.hostname, record.port);
            if (node == null) {
                return;
            }
            // todo: add node in nodes, might be merged with Servers
            nm.put(record.id, node);
        });
    }
}
