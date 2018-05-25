import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class LocalNode {
    private static final String defaultServiceName = "Node";
    private static final Logger log = LogManager.getLogger();

    boolean start(int port, Remote binding) {
        try {
            Registry localRegistry = LocateRegistry.createRegistry(port);
            localRegistry.bind(defaultServiceName, binding);
            return true;
        } catch (RemoteException | AlreadyBoundException e) {
            // todo: local node exception
            log.error("local node:", e);
            e.printStackTrace();
        }
        return false;
    }
}
