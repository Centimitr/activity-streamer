import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;


public class Control extends Thread {
    private static final Logger log = LogManager.getLogger();
    private static Gson g = new Gson();
    private ArrayList<Connectivity> clientConns;
    private Connectivity serverConn;
    private boolean term = false;
    private Listener listener;

    protected static Control control = null;

    public static Control getInstance() {
        if (control == null) {
            control = new Control();
        }
        return control;
    }

    public Control() {
        clientConns = new ArrayList<Connectivity>();
        setMessageHandlers();
        connectServerNode();
        startListen();
    }


    private void startListen() {
//        Connectivity self = this;
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
                serverConn = new Connectivity(Settings.getRemoteHostname(), Settings.getRemotePort(), c -> {

                });
            } catch (IOException e) {
                log.error("failed to make connection to " + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
                System.exit(-1);
            }
        }
    }

    private void setMessageHandlers() {
        MessageProtocol.getInstance()
                .registerHandler(MessageCommands.LOGOUT, context -> {
                    Message m = context.read(Message.class);
                    // {"command":"LOGOUT"}
                    log.info("@LOGOUT");
                    context.close();
                }).registerHandler(MessageCommands.INVALID_MESSAGE, context -> {
                    MessageInfo m = context.read(MessageInfo.class);
                    // {"command":"INVALID_MESSAGE", "info":"this is info"}
                    log.info("@INVALID_MESSAGE: " + m.info);
                }
        );
    }

    private void handleIncomingConn(Listener l, Socket s) {
        log.debug("incoming connection: " + Settings.socketAddress(s));

        try {
            Connectivity c = new Connectivity(s, conn -> {
                try {
                    boolean term = false;
                    String data;
                    while (!term && (data = conn.in.readLine()) != null) {
                        term = process(conn, data);
                    }
                    log.debug("connection closed to " + Settings.socketAddress(s));
                    connectionClosed(conn);
                    conn.in.close();
                } catch (IOException e) {
                    log.error("connection " + Settings.socketAddress(s) + " closed with exception: " + e);
                    connectionClosed(conn);
                }
            });
            clientConns.add(c);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Processing incoming messages from the connection.
     * Return true if the connection should close.
     */
    public synchronized boolean process(Connectivity c, String msg) {
        // todo: remove this debug use code
        if (!msg.startsWith("{")) {
            System.out.println("RCV: " + msg);
            c.sendln("R: " + msg);
            return false;
        }
        MessageContext mc = new MessageContext();
        boolean ok = mc.parse(msg);
        if (ok) {
            mc.process();
            c.sendln("RJ: " + msg);
            return mc.needClose();
        }
        return true;
    }

    /*
     * Connection Status Handling
     */
    public synchronized void connectionClosed(Connectivity c) {
        if (!term) clientConns.remove(c);
    }

//    public synchronized Connection incomingConnection(Socket s) throws IOException {
//        log.debug("incomming connection: " + Settings.socketAddress(s));
//        Connection c = new Connection(s);
//        connections.add(c);
//        return c;
//    }
//
//    public synchronized Connection outgoingConnection(Socket s) throws IOException {
//        log.debug("outgoing connection: " + Settings.socketAddress(s));
//        Connection c = new Connection(s);
//        connections.add(c);
//        return c;
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
