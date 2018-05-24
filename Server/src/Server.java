import org.w3c.dom.Node;

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

    private void init() {
        // accept remote invocations
        try {
            Registry localRegistry = LocateRegistry.createRegistry(Settings.getLocalPort());
            localRegistry.bind("Node", this);
        } catch (RemoteException | AlreadyBoundException e) {
            // todo: local node exception
            log.error("local node:", e);
            e.printStackTrace();
        }
        if (Settings.getRemoteHostname() != null) {
            // todo: connect parent, parent should add this node
            recoverLock.lock();
            IRemoteNode parent = connectNode(Settings.getRemoteHostname(), Settings.getRemotePort());
            if (parent == null) {
                System.exit(-1);
            }
            // todo: sync states
            // todo: connect all nodes
            recoverLock.until();
//            try {
//                Connectivity conn = new Connectivity(Settings.getRemoteHostname(), Settings.getRemotePort());
//                cm.parent().set(conn);
//                conn.whenClosed(() -> {
//                    log.info("Parent.Closed");
//                    terminate();
//                });
//                log.info("Authenticate.Start");
////                conn.sendln(new MsgAuthenticate(Settings.getSecret()));
//                conn.sendln(new MsgAuthenticate(Settings.getSecret()));
//            } catch (IOException e) {
//                log.error("Parent.Failed" + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
//                System.exit(-1);
//            }
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
////        log.info("Activity.Announce Load: " + load);
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
            cm.parent().close();
            log.info("Closed: " + cm.parent().size() + " parent, " + cm.clients().size() + " Clients, " + cm.children().size() + " Children");
            term = true;
            System.exit(0);
        }
    }
}
