import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;

@SuppressWarnings("WeakerAccess")
public class Control extends Thread {
    private static final Logger log = LogManager.getLogger();
    private static final Gson g = new Gson();

    private String uuid = UUID.randomUUID().toString();
    private ConnectivityManager manager = new ConnectivityManager();
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
                manager.parent().set(conn);
                (new Thread(() -> {
                    boolean closed = conn.redirect(manager.parent().router());
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

    private void setMessageHandlers() {
        manager.temp().router()
                .registerHandler(MessageCommands.LOGIN, context -> {
                    manager.temp().transfer(context.connectivity, manager.clients());
                });
        manager.clients().router()
                .registerHandler(MessageCommands.LOGOUT, context -> {
                    Message m = context.read(Message.class);
                    // {"command":"LOGOUT"}
                    log.info("@LOGOUT");
                    context.close();
                })
                .registerHandler(MessageCommands.INVALID_MESSAGE, context -> {
                    MessageInfo m = context.read(MessageInfo.class);
                    // {"command":"INVALID_MESSAGE", "info":"this is info"}

                    log.info("@INVALID_MESSAGE: " + m.info);
                })
                .registerHandler(MessageCommands.ACTIVITY_MESSAGE, context -> {
                    MessageActivity m = context.read(MessageActivity.class);
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
                        MessageInfo res = new MessageInfo(MessageCommands.AUTHTENTICATION_FAIL.name(), info);
                        context.write(res);
                        context.close();
                        return;
                    }
                    // todo: refactor
                    // todo: INVALID_MESSAGE, incorrect in anyway
//                    JsonObject activity = new JsonParser().parse(m.activity).getAsJsonObject();
//                     todo: need check if the activity(JsonObject) can be marshaled correctly
//                    activity.addProperty("authenticated_user", context.get("username"));
//                    MessageActivityBroadcast broadcast = new MessageActivityBroadcast(
//                            MessageCommands.ACTIVITY_BROADCAST.name(),
//                            g.toJson(activity)
//                    );
//                    broadcastToServers(broadcast);
                })
                .registerErrorHandler(c -> {

                });
        manager.children().router()
                .registerHandler(MessageCommands.ACTIVITY_BROADCAST, context -> {
                    // todo: INVALID_MESSAGE, incorrect in anyway
                    // todo: received from an unauthenticated server
                    if (false) {
                        context.close();
                        return;
                    }
                    JsonObject m = context.read();
                    manager.all().exclude(context.connectivity).broadcast(m);
                })
                .registerHandler(MessageCommands.SERVER_ANNOUNCE, context -> {
                    JsonObject m = context.read();
                    manager.servers().exclude(context.connectivity).broadcast(m);
                })
                .registerHandler(MessageCommands.AUTHTENTICATION_FAIL, context -> {
                })
                .registerHandler(MessageCommands.INVALID_MESSAGE, context -> {
                })
                .registerErrorHandler(c -> {

                });
    }

    // todo: check if synchronized is appropriate
    private synchronized void startAuthentication(Connectivity c) {
        boolean ok;
        ok = c.sendln(new MessageSecret(MessageCommands.AUTHENTICATE.name(), Settings.getSecret()));
        log.info("Authentication: " + ok);
        // todo: if error happens in the S/S process, maybe disconnect?
    }

    private void handleIncomingConn(Listener l, Socket s) {
        try {
            Connectivity c = new Connectivity(s, con -> {
                MessageContext ctx = new MessageContext(manager.temp().router());
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
            manager.temp().add(c);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClosedConn(Connectivity c) {
        if (!term) {
            ConnectivitySet ownerSet = manager.all().owner(c);
            if (ownerSet != null) {
                ownerSet.remove(c);
            }
        }
    }

    @Override
    public void run() {
        log.info("using activity interval of " + Settings.getActivityInterval() + " milliseconds");
        while (!term) {
            // do something with 5 second intervals in between
            try {
                Thread.sleep(Settings.getActivityInterval());
            } catch (InterruptedException e) {
                log.info("received an interrupt, system is shutting down");
                break;
            }
            if (!term) {
                log.debug("doing activity");
                term = doActivity();
            }
        }
        log.info("closing " + manager.clients().size() + " client connections.");
        log.info("closing " + manager.children().size() + " server connections.");
        // clean up
        manager.all().sets().forEach(ConnectivitySet::closeAll);
        log.info("closing parent server connection.");
        listener.terminate();
    }

    public boolean doActivity() {
        System.out.println("DoActivity!");
        doServerAnnounce();
        return false;
    }

    private void doServerAnnounce() {
        Integer load = manager.clients().size();
        MessageServerAnnounce m = new MessageServerAnnounce(
                MessageCommands.SERVER_ANNOUNCE.name(),
                Settings.getLocalHostname(),
                Settings.getLocalPort(),
                uuid,
                load
        );
        manager.servers().broadcast(m);
    }

    public final void terminate() {
        term = true;
    }
}
