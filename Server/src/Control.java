import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("WeakerAccess")
public class Control extends Thread {
    private static final Logger log = LogManager.getLogger();
    private static Gson g = new Gson();

    private MessageRouter clientMessageRouter = new MessageRouter();
    private MessageRouter serverMessageRouter = new MessageRouter();
    private ArrayList<Connectivity> clientConns = new ArrayList<>();
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

    private void setMessageHandlers() {
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
                .registerErrorHandler(c -> {

                });
        serverMessageRouter
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
        ok = c.redirect((conn, msg) -> (new MessageContext(serverMessageRouter)).process(conn, msg));
        // todo: if error happens in the S/S process, maybe disconnect?
    }

    private synchronized void handleIncomingConn(Listener l, Socket s) {
        try {
            Connectivity c = new Connectivity(s, con -> {
//                boolean ok = conn.redirect(this::handleClientMessage);
                boolean ok = con.redirect((conn, msg) -> {
                    // todo: remove this debug use code
                    if (!msg.startsWith("{")) {
                        System.out.println("RCV: " + msg);
                        conn.sendln("R: " + msg);
                        return false;
                    }
                    return (new MessageContext(clientMessageRouter)).process(conn, msg);
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
        return false;
    }

    public final void terminate() {
        term = true;
    }

//    public final ArrayList<Connection> getConnections() {
//        return connections;
//    }
}
