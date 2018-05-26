import java.io.IOException;
import java.net.Socket;
import java.rmi.*;
import java.util.Map;

// todo: exception when sending data vai a closed connection

@SuppressWarnings("WeakerAccess")
public class Server extends ServerResponder {

    private Listener listener;
    private boolean term = false;
    final Thread loadUpdateDaemon = (new Thread(this::updateLoadNotify));
    final Thread eventualBalanceDaemon = (new Thread(this::checkPossibleBalance));

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
        startListen();
        loadUpdateDaemon.run();
        eventualBalanceDaemon.run();
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
            String recoveryDataJson = parent.get().declare(Settings.getSecret(), Settings.getLocalHostname(), Settings.getLocalPort(), true);
            // recover from recovery data
            RecoveryData data = RecoveryData.fromJson(recoveryDataJson);
            rm.recover(data.getRegisteredAccounts());
            for (EndPoint endPoint : data.getNodesToConnect()) {
                RemoteNode node = nm.add(endPoint.hostname, endPoint.port);
                log.info("Connected: " + endPoint.hostname + ":" + endPoint.port);
                if (node != null) {
                    node.get().declare(Settings.getSecret(), Settings.getLocalHostname(), Settings.getLocalPort(), false);
                }
            }
            recoverLock.unlock();
        }
    }

    private void startListen() {
        try {
            listener = new Listener(Settings.getClientPort(), this::handleIncomingConn);
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

    public void updateLoadNotify() {
        log.info("Activity.Start Interval: " + Settings.getActivityInterval());
        while (true) {
            nm.updateLoads();
            try {
                Thread.sleep(Settings.getActivityInterval());
            } catch (InterruptedException e) {
                log.info("Interrupt");
                break;
            }
        }
    }

    public void checkPossibleBalance() {
        while (true) {
            RemoteNode freeNode = nm.getFreeNode();
            if (freeNode != null) {
                int transferNumber = (freeNode.getLoad() - nm.local().getLoad()) / 2;
                int transferredNumber = 0;
                for (Map.Entry<String, Connectivity> entry : sm.getConnectivities().entrySet()) {
                    String username = entry.getKey();
                    Connectivity conn = entry.getValue();
                    conn.sendln(new MsgRedirect(freeNode.hostname, freeNode.port));
                    sm.markAsOffline(username);
                    conn.close();
                    transferredNumber++;
                    if (transferredNumber >= transferNumber) {
                        break;
                    }
                }
            }
            try {
                Thread.sleep(Env.CHECK_BALANCE_INTERVAL);
            } catch (InterruptedException e) {
                log.info("Interrupt");
                break;
            }
        }

    }

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
