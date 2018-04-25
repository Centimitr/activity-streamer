import java.io.IOException;
import java.net.Socket;

// todo: exception when sending data vai a closed connection

@SuppressWarnings("WeakerAccess")
public class Server extends ServerResponder {

    private Listener listener;
    private boolean term = false;

    protected static Server control = null;

    public static Server getInstance() {
        if (control == null) {
            control = new Server();
        }
        return control;
    }

    public Server() {
        connectParent();
        startListen();
        start();
    }

    private void connectParent() {
        if (Settings.getRemoteHostname() != null) {
            try {
                Connectivity conn = new Connectivity(Settings.getRemoteHostname(), Settings.getRemotePort());
                cm.parent().set(conn);
                conn.whenClosed(() -> {
                    log.info("Parent.Closed");
                    terminate();
                });
                log.info("Authenticate.Start");
                conn.sendln(new MsgAuthenticate(Settings.getSecret()));
            } catch (IOException e) {
                log.error("Parent.Failed" + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
                System.exit(-1);
            }
        }
    }

    private void startListen() {
        try {
            listener = new Listener(Settings.getLocalPort(), this::handleIncomingConn);
        } catch (IOException e) {
            log.fatal("Listener.Failed " + e);
            System.exit(-1);
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
        log.info("Activity.Start Interval: " + Settings.getActivityInterval());
        while (!term) {
            term = doActivity();
            if (!term) {
                try {
                    Thread.sleep(Settings.getActivityInterval());
                } catch (InterruptedException e) {
                    log.info("Interrupt");
                    break;
                }
            }
        }
    }

    public boolean doActivity() {
        doServerAnnounce();
        return false;
    }

    private void doServerAnnounce() {
        Integer load = cm.clients().size();
//        log.info("Activity.Announce Load: " + load);
        MsgServerAnnounce m = new MsgServerAnnounce(
                uuid,
                Settings.getLocalHostname(),
                Settings.getLocalPort(),
                load
        );
        cm.servers().broadcast(m);
    }

    public final void terminate() {
        if (!term) {
            listener.terminate();
            cm.all().sets().forEach(ConnectivitySet::closeAll);
            cm.parent().close();
            log.info("Closed: " + cm.parent().size() + " parent, " + cm.clients().size() + " Clients, " + cm.children().size() + " Children");
            term = true;
            System.exit(0);
        }
    }
}
