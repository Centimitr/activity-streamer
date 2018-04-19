import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;

@SuppressWarnings("WeakerAccess")
public class Control extends Thread {
    private static final Logger log = LogManager.getLogger();
    private static final Gson g = new Gson();

    private String uuid = UUID.randomUUID().toString();
    private ConnectivityManager manager = new ConnectivityManager();
    private MessageRouter tempMessageRouter = new MessageRouter();
    private MessageRouter clientMessageRouter = new MessageRouter();
    private MessageRouter serverMessageRouter = new MessageRouter();
    private ArrayList<Connectivity> clientConns = new ArrayList<>();
    private ArrayList<Connectivity> serverConns = new ArrayList<>();
    private Connectivity serverConn;
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
        connectServerNode();
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

    private void connectServerNode() {
        if (Settings.getRemoteHostname() != null) {
            try {
                serverConn = new Connectivity(Settings.getRemoteHostname(), Settings.getRemotePort(), this::startAuthentication);
            } catch (IOException e) {
                log.error("failed to make connection to " + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
                System.exit(-1);
            }
        }
    }

    private void serverConnsForEach(Consumer<Connectivity> fn) {
        if (serverConn != null) {
            fn.accept(serverConn);
        }
        serverConns.forEach(fn);
    }

    private void serverConnsForEachExclude(Connectivity toExclude, Consumer<Connectivity> fn) {
        serverConnsForEach(conn -> {
            if (!conn.equals(toExclude)) {
                fn.accept(conn);
            }
        });
    }

    private void broadcastToServers(Object msg) {
        serverConnsForEach(conn -> conn.sendln(msg));
    }

    private void broadcastToServers(Object msg, Connectivity toExclude) {
        serverConnsForEachExclude(toExclude, conn -> conn.sendln(msg));
    }

    private void broadcastToServers(Object msg, MessageContext toExclude) {
        serverConnsForEachExclude(toExclude.connectivity, conn -> conn.sendln(msg));
    }

    private void broadcastToClients(Object msg) {
        clientConns.forEach(conn -> conn.sendln(msg));
    }

    private void setMessageHandlers() {
        tempMessageRouter
                .registerHandler(MessageCommands.LOGIN, context -> {
                    manager.temp().transfer(context.connectivity, manager.clients());
                });
        clientMessageRouter
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
                    // todo: INVALID_MESSAGE, incorrect in anyway
                    JsonObject activity = new JsonParser().parse(m.activity).getAsJsonObject();
                    // todo: need check if the activity(JsonObject) can be marshaled correctly
                    activity.addProperty("authenticated_user", context.get("username"));
                    MessageActivityBroadcast broadcast = new MessageActivityBroadcast(
                            MessageCommands.ACTIVITY_BROADCAST.name(),
                            g.toJson(activity)
                    );
                    broadcastToServers(broadcast);
                })
                .registerErrorHandler(c -> {

                });
        serverMessageRouter
                .registerHandler(MessageCommands.ACTIVITY_BROADCAST, context -> {
                    // todo: INVALID_MESSAGE, incorrect in anyway
                    // todo: received from an unauthenticated server
                    if (false) {
                        context.close();
                        return;
                    }
                    JsonObject m = context.read();
                    broadcastToServers(m, context);
                    broadcastToClients(m);
                })
                .registerHandler(MessageCommands.SERVER_ANNOUNCE, context -> {
                    JsonObject m = context.read();
                    broadcastToServers(m, context);
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
//        ok = c.redirect(this::handleServerMessage);
//        ok = c.redirect((conn, msg) -> (new MessageContext(serverMessageRouter)).process(conn, msg));
        ok = c.redirect(serverMessageRouter);
        // todo: if error happens in the S/S process, maybe disconnect?
    }

    private synchronized void handleIncomingConn(Listener l, Socket s) {
        try {
            Connectivity c = new Connectivity(s, con -> {
//                boolean ok = conn.redirect(this::handleClientMessage);
                MessageContext ctx = new MessageContext(tempMessageRouter);
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
            clientConns.add(c);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void handleClosedConn(Connectivity c) {
        if (!term) clientConns.remove(c);
    }

//    private synchronized boolean handleClientMessage(Connectivity c, String msg) {
//        // todo: remove this debug use code
//        if (!msg.startsWith("{")) {
//            System.out.println("RCV: " + msg);
//            c.sendln("R: " + msg);
//            return false;
//        }
//        return (new MessageContext(clientMessageRouter)).process(c, msg);
//    }
//
//    private synchronized boolean handleServerMessage(Connectivity c, String msg) {
//        return (new MessageContext(serverMessageRouter)).process(c, msg);
//    }

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
        log.info("closing " + clientConns.size() + " connections");
        // clean up
        for (Connectivity c : clientConns) {
            c.close();
        }
        if (serverConn != null) {
            serverConn.close();
        }
        listener.terminate();
    }

    public boolean doActivity() {
        System.out.println("DoActivity!");
        doServerAnnounce();
        return false;
    }

    private void doServerAnnounce() {
        Integer load = clientConns.size();
        MessageServerAnnounce m = new MessageServerAnnounce(
                MessageCommands.SERVER_ANNOUNCE.name(),
                Settings.getLocalHostname(),
                Settings.getLocalPort(),
                uuid,
                load
        );
        serverConnsForEach(conn -> conn.sendln(m));
    }

    public final void terminate() {
        term = true;
    }

//    public final ArrayList<Connection> getConnections() {
//        return connections;
//    }
}
