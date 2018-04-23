import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class Base extends Async {
    static final Logger log = LogManager.getLogger();
    static final Gson g = new Gson();

    String uuid = UUID.randomUUID().toString();
    ConnectivityManager cm = new ConnectivityManager();
    @SuppressWarnings("WeakerAccess")
    RegisterManager rm = new RegisterManager();
    @SuppressWarnings("WeakerAccess")
    Servers servers = new Servers(cm.servers());
    @SuppressWarnings("WeakerAccess")
    Users users = new Users();

    Base() {
        RouterManager routers = cm.routerManager();
        routers.temp()
                .registerHandler(MessageCommands.AUTHENTICATE, context -> {
                    MsgAuthenticate m = context.read(MsgAuthenticate.class);
                    boolean success = m.secret.equals(Settings.getSecret());
                    if (!success) {
                        MsgAuthenticationFail res = new MsgAuthenticationFail("the supplied secret is incorrect:" + m.secret);
                        context.write(res);
                        context.close();
                    }
                })
                .registerHandler(MessageCommands.LOGIN, context -> {
                    MsgLogin m = context.read(MsgLogin.class);
                    boolean match = users.match(m.username, m.secret);
                    if (!match) {
                        context.write(new MsgLoginFailed("attempt to login with wrong secret"));
                        context.close();
                        return;
                    }
                    context.write(new MsgLoginSuccess("logged in as user " + m.username));
                    cm.temp().transfer(context.connectivity, cm.clients());
                    // todo: load balance: redirect
                    int currentLoad = cm.clients().size();
                    ServerRecord availableServer = servers.balancer().getAvailableServer(currentLoad);
                    boolean needRedirect = availableServer != null;
                    if (needRedirect) {
                        context.write(new MsgRedirect(availableServer.hostname, availableServer.port));
                        context.close();
                    }
                })
                .registerErrorHandler(context -> {
                            context.write(new MsgInvalidMessage("INVALID MESSAGE?"));
                        }
                );
        routers.register()
                .registerHandler(MessageCommands.REGISTER, context -> {
                    MsgRegister m = context.read(MsgRegister.class);
                    boolean registered = users.has(m.username);
                    // todo：need test
                    Runnable handleRegisteredRequest = () -> {
                        String info = m.username + " is already registered with the system.";
                        MsgRegisterFailed res = new MsgRegisterFailed(info);
                        context.write(res);
                        context.close();
                    };
                    if (registered) {
                        handleRegisteredRequest.run();
                        return;
                    }
                    // ask other servers' options
                    // broadcast lock request to all other servers, and wait for responds
                    MsgLockRequest req = new MsgLockRequest(m.username, m.secret);
                    cm.servers().broadcast(req);
                    boolean possibleRegistered = rm.wait(m.username, m.secret, servers.num());
                    if (!possibleRegistered) {
                        handleRegisteredRequest.run();
                        return;
                    }
                    String info = "register success for " + m.username;
                    MsgRegisterSuccess res = new MsgRegisterSuccess(info);
                    context.write(res);
                    // todo: move to clients
                })
                .registerErrorHandler(c -> {
                });
        routers.client()
                .registerHandler(MessageCommands.LOGOUT, context -> {
                    context.close();
                })
                .registerHandler(MessageCommands.ACTIVITY_MESSAGE, context -> {
                    MsgActivityMessage m = context.read(MsgActivityMessage.class);
                    boolean anonymous = m.username.equals("anonymous");
                    boolean match = m.username.equals(context.get("username")) && m.secret.equals(context.get("secret"));
                    boolean loggedIn = context.get("username") != null;
                    if (!anonymous || !match || !loggedIn) {
                        String info = "";
                        if (!anonymous || !loggedIn) {
                            info = "username is not anonymous or no user logged in.";
                        }
                        if (!match) {
                            info = "the supplied secret is incorrect: " + m.secret;
                        }
                        MessageInfo res = new MsgAuthenticationFail(info);
                        context.write(res);
                        context.close();
                        return;
                    }
                    // todo: INVALID_MESSAGE, incorrect in anyway
                    // todo: need check if the activity(JsonObject) can be marshaled correctly
                    m.activity.addProperty("authenticated_user", context.get("username"));
                    MsgActivityBroadcast broadcast = new MsgActivityBroadcast(
                            g.toJson(m.activity)
                    );
                    cm.servers().broadcast(broadcast);
                })
                .registerErrorHandler(c -> {

                });
        routers.parent()
                .registerHandler(MessageCommands.AUTHENTICATION_FAIL, context -> {
                })
                .registerErrorHandler(c -> {

                });
        routers.server()
                .registerHandler(MessageCommands.ACTIVITY_BROADCAST, context -> {
                    // todo: INVALID_MESSAGE, incorrect in anyway
                    // todo: received from an unauthenticated server
                    if (false) {
                        context.close();
                        return;
                    }
                    JsonObject m = context.read();
                    cm.all().exclude(context.connectivity).broadcast(m);
                })
                .registerHandler(MessageCommands.SERVER_ANNOUNCE, context -> {
                    MsgServerAnnounce m = context.read(MsgServerAnnounce.class);
                    servers.records().put(m.id, m.hostname, m.port, m.load);
                    cm.servers().exclude(context.connectivity).broadcast(m);
                })
                .registerHandler(MessageCommands.LOCK_REQUEST, context -> {
                    MsgLockRequest req = context.read(MsgLockRequest.class);
                    boolean match = users.match(req.username, req.secret);
                    if (!match) {
                        //broadcast a lock-denied to all other servers
                        MsgLockDenied res = new MsgLockDenied(req.username, req.secret);
                        cm.servers().exclude(context.connectivity).broadcast(res);
                    } else if (!users.has(req.username)) {
                        MsgLockAllowed res = new MsgLockAllowed(req.username, req.secret);
                        cm.servers().exclude(context.connectivity).broadcast(res);
                        users.add(req.username, req.secret);
                    }
                })
                .registerHandler(MessageCommands.LOCK_ALLOWED, context -> {
                    MsgLockAllowed req = context.read(MsgLockAllowed.class);
                    //todo: what actions to be considered
//                    response.add(context.connectivity, req);
                })
                .registerHandler(MessageCommands.LOCK_DENIED, context -> {
                    MsgLockDenied req = context.read(MsgLockDenied.class);
                    // todo:
                    users.delete(req.username, req.secret);
//                    response.add(context.connectivity, req);
                })
                .registerHandler(MessageCommands.INVALID_MESSAGE, context -> {
                })
                .registerErrorHandler(c -> {

                });
    }
}
