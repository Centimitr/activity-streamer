import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

@SuppressWarnings("WeakerAccess")
class RemoteNode extends Node {
    String id;
    final String hostname;
    final int port;
    final String serviceName;

    RemoteNode(String hostname, int port, String name) {
        this.hostname = hostname;
        this.port = port;
        this.serviceName = name;
    }

    RemoteNode(String hostname, int port) {
        this(hostname, port, RemoteNode.defaultServiceName);
    }

    boolean connect() {
        try {
            Registry remoteRegistry = LocateRegistry.getRegistry(hostname, port);
            node = (IRemoteNode) remoteRegistry.lookup(serviceName);
            return true;
        } catch (RemoteException | NotBoundException e) {
            // todo: maybe remote fails
            e.printStackTrace();
        }
        return false;
    }
}
