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
    private static ArrayList<Connection> connections;
    private static boolean term = false;
    private static Listener listener;
    private Gson g = new Gson();

    protected static Control control = null;

    public static Control getInstance() {
        if (control == null) {
            control = new Control();
        }
        return control;
    }

    public Control() {
        // initialize the connections array
        connections = new ArrayList<Connection>();
        // register message handlers
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
        // start a listener
        try {
            listener = new Listener();
        } catch (IOException e1) {
            log.fatal("failed to startup a listening thread: " + e1);
            System.exit(-1);
        }
    }

    public void initiateConnection() {
        // make a connection to another server if remote hostname is supplied
        if (Settings.getRemoteHostname() != null) {
            try {
                outgoingConnection(new Socket(Settings.getRemoteHostname(), Settings.getRemotePort()));
            } catch (IOException e) {
                log.error("failed to make connection to " + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
                System.exit(-1);
            }
        }
    }

    /*
     * Processing incoming messages from the connection.
     * Return true if the connection should close.
     */
    public synchronized boolean process(Connection con, String msg) {
        // test function
        if (!msg.startsWith("{")) {
            System.out.println("RCV: " + msg);
            con.writeMsg("R: " + msg + '\n');
            return false;
        }
        MessageContext mc = new MessageContext();
        boolean ok = mc.parse(msg);
        if (ok) {
            mc.process();
            return mc.needClose();
        }
        return true;
    }

    /*
     * The connection has been closed by the other party.
     */
    public synchronized void connectionClosed(Connection con) {
        if (!term) connections.remove(con);
    }

    /*
     * A new incoming connection has been established, and a reference is returned to it
     */
    public synchronized Connection incomingConnection(Socket s) throws IOException {
        log.debug("incomming connection: " + Settings.socketAddress(s));
        Connection c = new Connection(s);
        connections.add(c);
        return c;
    }

    /*
     * A new outgoing connection has been established, and a reference is returned to it
     */
    public synchronized Connection outgoingConnection(Socket s) throws IOException {
        log.debug("outgoing connection: " + Settings.socketAddress(s));
        Connection c = new Connection(s);
        connections.add(c);
        return c;
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
        log.info("closing " + connections.size() + " connections");
        // clean up
        for (Connection connection : connections) {
            connection.closeCon();
        }
        listener.setTerm(true);
    }

    public boolean doActivity() {
        System.out.println("DoActivity!");
        return false;
    }

    public final void setTerm(boolean t) {
        term = t;
    }

    public final ArrayList<Connection> getConnections() {
        return connections;
    }
}
