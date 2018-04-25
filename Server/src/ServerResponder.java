import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

@SuppressWarnings("WeakerAccess")
abstract class ServerResponder extends Async {
    static final Logger log = LogManager.getLogger();
    static final Gson g = new Gson();

    String uuid = UUID.randomUUID().toString();
    ConnectivityManager cm = new ConnectivityManager();
    RegisterManager rm = new RegisterManager();
    Servers servers = new Servers(cm.servers());
    Users users = new Users();

    @SuppressWarnings({"CodeBlock2Expr", "Convert2MethodRef"})
    ServerResponder() {
        RouterManager routers = cm.routerManager();
        // todo: handleError need an error parameter
        routers.temp()
                .handle(MessageCommands.AUTHENTICATE, context -> {
                    MsgAuthenticate m = context.read(MsgAuthenticate.class);
                    boolean success = m.secret.equals(Settings.getSecret());
                    if (!success) {
                        MsgAuthenticationFail res = new MsgAuthenticationFail("the supplied secret is incorrect:" + m.secret);
                        context.write(res);
                        context.close();
                    }
                    cm.temp().transfer(context.connectivity, cm.children());
                })
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
                    cm.servers().broadcast(req);
                    boolean available = rm.wait(m.username, m.secret, servers.num());
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
                .handleError(context -> {
                    log.error("temp: HANDLE ERROR");
                    context.write(new MsgInvalidMessage("INVALID MESSAGE?"));
                    context.close();
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
                        JsonObject activity = (new JsonParser()).parse(m.activity).getAsJsonObject();
                        activity.addProperty("authenticated_user", context.get("username"));
                        MsgActivityBroadcast broadcast = new MsgActivityBroadcast(
                                g.toJson(activity)
                        );
                        cm.servers().broadcast(broadcast);
                        cm.clients().broadcast(broadcast);
                    } catch (JsonSyntaxException e) {
                        MsgInvalidMessage res = new MsgInvalidMessage("activity object json syntax error");
                        context.write(res);
                        context.close();
                    }
                })
                .handleError(context -> {
                    log.error("client: HANDLE ERROR");
                    context.write(new MsgInvalidMessage("INVALID MESSAGE?"));
                    context.close();
                });
        routers.parent()
                .handle(MessageCommands.AUTHENTICATION_FAIL, context -> {
                    context.close();
                })
                .handle(MessageCommands.INVALID_MESSAGE, context -> {
                    log.error("RCV INVALID MESSAGE");
                })
                .handleError(context -> {
                    log.error("parent: HANDLE ERROR");
                    context.write(new MsgInvalidMessage("INVALID MESSAGE?"));
                    context.close();
                });
        routers.child()
                .handle(MessageCommands.AUTHENTICATE, context -> {
                    context.write(new MsgInvalidMessage("Server already authenticated"));
                    context.close();
                })
                .handleError(context -> {
                    log.error("child: HANDLE ERROR");
                    context.write(new MsgInvalidMessage("INVALID MESSAGE?"));
                    context.close();
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
        routers.server()
                .handle(MessageCommands.ACTIVITY_BROADCAST, context -> {
                    JsonObject m = context.read();
                    cm.servers().exclude(context.connectivity).broadcast(m);
                    cm.clients().broadcast(m);
                })
                .handle(MessageCommands.SERVER_ANNOUNCE, context -> {
                    MsgServerAnnounce m = context.read(MsgServerAnnounce.class);
                    servers.records().put(m.id, m.hostname, m.port, m.load);
                    cm.servers().exclude(context.connectivity).broadcast(m);
                })
                .handle(MessageCommands.LOCK_REQUEST, context -> {
                    // broadcast
                    cm.servers().exclude(context.connectivity).broadcast(context.read());
                    // handle request
                    MsgLockRequest req = context.read(MsgLockRequest.class);
                    boolean known = users.has(req.username);
                    boolean match = users.match(req.username, req.secret);
                    if (known) {
                        MsgLockDenied res = new MsgLockDenied(req.username, req.secret);
                        cm.servers().broadcast(res);
                        if (match) {
                            users.delete(req.username, req.secret);
                        }
                        return;
                    }
                    users.add(req.username, req.secret);
                    MsgLockAllowed res = new MsgLockAllowed(req.username, req.secret);
                    cm.servers().broadcast(res);
                })
                .handle(MessageCommands.LOCK_ALLOWED, context -> {
                    cm.servers().exclude(context.connectivity).broadcast(context.read());
                })
                .handle(MessageCommands.LOCK_DENIED, context -> {
                    // broadcast
                    cm.servers().exclude(context.connectivity).broadcast(context.read());
                    // handle denied
                    MsgLockDenied m = context.read(MsgLockDenied.class);
                    boolean match = users.match(m.username, m.secret);
                    if (match) {
                        users.delete(m.username, m.secret);
                    }
                });
    }
}
