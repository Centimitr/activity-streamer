import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// todo: exception when sending data vai a closed connection

@SuppressWarnings("WeakerAccess")
public class Control extends Thread {
    private static final Logger log = LogManager.getLogger();
    private static final Gson g = new Gson();

    private String uuid = UUID.randomUUID().toString();
    private ConnectivityManager cm = new ConnectivityManager();
    private RegisterManager rm = new RegisterManager();
    private Servers servers = new Servers(cm.servers());
    private Users users = new Users();
    private Listener listener;
    private boolean term = false;

    protected static Control control = null;

    public static Control getInstance() {
        if (control == null) {
            control = new Control();
        }
        return control;
    }

    public Control() {
        setMessageHandlers();
        connectParent();
        startListen();
        start();
    }

    private void startListen() {
        try {
            listener = new Listener(Settings.getLocalPort(), this::handleIncomingConn);
        } catch (IOException e) {
            log.fatal("failed to startup a listening thread: " + e);
            System.exit(-1);
        }
    }

    private void connectParent() {
        if (Settings.getRemoteHostname() != null) {
            try {
                Connectivity conn = new Connectivity(Settings.getRemoteHostname(), Settings.getRemotePort(), this::startAuthentication);
                cm.parent().set(conn);
                (new Thread(() -> {
                    boolean closed = conn.redirect(cm.parent().router());
                    if (closed) {
                        log.info("Parent connection closed!");
                    }
                })).start();
            } catch (IOException e) {
                log.error("failed to make connection to " + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
                System.exit(-1);
            }
        }
    }

    @SuppressWarnings({"CodeBlock2Expr", "Convert2MethodRef"})
    private void setMessageHandlers() {
        cm.temp().router()
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
                    boolean needRedirect = false;
                    if (needRedirect) {
//                        context.write(new MsgRedirect("logged in as user " + m.username));
                        context.close();
                    }
                }).registerErrorHandler(context -> {
                    context.write(new MsgInvalidMessage("INVALID MESSAGE?"));
                }
        );
        cm.clients().router()
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
                })
                .registerErrorHandler(c -> {

                });
        cm.children().router()
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
                .registerHandler(MessageCommands.AUTHENTICATION_FAIL, context -> {
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

    // todo: check if synchronized is appropriate
    private synchronized void startAuthentication(Connectivity c) {
        c.sendln(new MsgAuthenticate(Settings.getSecret()));
        log.info("Start Authentication!");
    }

    private void handleIncomingConn(Socket s) {
        try {
            Connectivity c = new Connectivity(s, con -> {
                MessageContext ctx = new MessageContext(cm.temp().router());
                boolean ok = con.redirect((conn, msg) -> {
                    // todo: remove this debug use code
                    if (!msg.startsWith("{")) {
                        System.out.println("RCV: " + msg);
                        conn.sendln("R: " + msg);
                        return false;
                    }
                    return ctx.process(conn, msg);
                });
                if (!ok) {
                    log.debug("connection closed to " + Settings.socketAddress(s));
                    handleClosedConn(con);
                    // todo: find a good time to close in stream to ensure the I/O order
                    // conn.in.close();
                }
            });
            cm.temp().add(c);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClosedConn(Connectivity c) {
        if (!term) {
            ConnectivitySet ownerSet = cm.all().owner(c);
            if (ownerSet != null) {
                ownerSet.remove(c);
            }
        }
    }

    @Override
    public void run() {
        log.info("using activity interval of " + Settings.getActivityInterval() + " milliseconds");
        while (!term) {
            log.debug("doing activity");
            term = doActivity();
            if (!term) {
                try {
                    Thread.sleep(Settings.getActivityInterval());
                } catch (InterruptedException e) {
                    log.info("received an interrupt, system is shutting down");
                    break;
                }
            }
        }
        log.info("closing " + cm.clients().size() + " client connections.");
        log.info("closing " + cm.children().size() + " server connections.");
        // clean up
        cm.all().sets().forEach(ConnectivitySet::closeAll);
        log.info("closing parent server connection.");
        listener.terminate();
    }

    public boolean doActivity() {
        System.out.println("DoActivity!");
        doServerAnnounce();
        return false;
    }

    private void doServerAnnounce() {
        Integer load = cm.clients().size();
        MsgServerAnnounce m = new MsgServerAnnounce(
                uuid,
                Settings.getLocalHostname(),
                Settings.getLocalPort(),
                load
        );
        cm.servers().broadcast(m);
    }

    public final void terminate() {
        term = true;
    }
}
