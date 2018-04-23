import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.jmx.Server;

// todo: exception when sending data vai a closed connection

@SuppressWarnings("WeakerAccess")
public class Control extends Base {

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
                Connectivity conn = new Connectivity(Settings.getRemoteHostname(), Settings.getRemotePort());
                cm.parent().set(conn);
                async(() -> {
                    boolean closed = conn.redirect(cm.routerManager().parent());
                    if (closed) {
                        log.info("Parent connection closed!");
                    }
                });
                async(() -> {
                    conn.sendln(new MsgAuthenticate(Settings.getSecret()));
                    log.info("Start Authentication!");
                });
            } catch (IOException e) {
                log.error("failed to make connection to " + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
                System.exit(-1);
            }
        }
    }

    private void handleIncomingConn(Socket s) {
        try {
            cm.temp().add(new Connectivity(s));
        } catch (IOException e) {
            e.printStackTrace();
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
