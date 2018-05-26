import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class LocalNode extends Node {
    private ConnectivitySet binding;

    boolean start(int port, Remote binding) {
        try {
            Registry localRegistry = LocateRegistry.createRegistry(port);
            localRegistry.bind(defaultServiceName, binding);
            node = (IRemoteNode) binding;
            return true;
        } catch (RemoteException | AlreadyBoundException e) {
            // todo: local node exception
            log.error("local node:", e);
            e.printStackTrace();
        }
        return false;
    }

    void bindConnectivitySet(ConnectivitySet set) {
        binding = set;
    }

    @Override
    int getLoad() {
        if (binding == null) {
            return super.getLoad();
        }
        return binding.effectiveSize();
    }

}
