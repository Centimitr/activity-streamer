import java.io.IOException;
import java.net.Socket;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

// todo: exception when sending data vai a closed connection

@SuppressWarnings("WeakerAccess")
public class Server extends ServerResponder {

    private Listener listener;
    private boolean term = false;

    protected static Server control = null;

    public static Server getInstance() {
        if (control == null) {
            try {
                control = new Server();
            } catch (RemoteException e) {
                log.fatal(e);
                System.exit(-1);
            }
        }
        return control;
    }

    public Server() throws RemoteException {
        super();
        init();
//        startListen();
        (new Thread(this::run)).run();
    }

    private void init() throws RemoteException {
        // accept remote invocations
        nm.local().start(Settings.getLocalPort(), this);
        // connect remote parent node
        if (Settings.getRemoteHostname() != null) {
            recoverLock.lock();
            RemoteNode parent = nm.add(Settings.getRemoteHostname(), Settings.getRemotePort());
            log.info("Connected: " + Settings.getRemoteHostname() + ":" + Settings.getRemotePort());
            if (parent == null) {
                System.exit(-1);
            }
            parent.get().declare(Settings.getSecret(), Settings.getRemoteHostname(), Settings.getRemotePort());
            recoverLock.until();
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
//        doServerAnnounce();
        return false;
    }

//    private void doServerAnnounce() {
//        Integer load = cm.clients().size();
////        log.info("-Activity.Announce Load: " + load);
//        MsgServerAnnounce m = new MsgServerAnnounce(
//                uuid,
//                Settings.getLocalHostname(),
//                Settings.getLocalPort(),
//                load
//        );
//        cm.servers().broadcast(m);
//    }

    public final void terminate() {
        if (!term) {
            listener.terminate();
            cm.all().sets().forEach(ConnectivitySet::closeAll);
//            cm.parent().close();
//            log.info("Closed: " + cm.parent().size() + " parent, " + cm.clients().size() + " Clients, " + cm.children().size() + " Children");
            term = true;
            System.exit(0);
        }
    }
}
