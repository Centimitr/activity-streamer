import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

@SuppressWarnings("WeakerAccess")
class RemoteNode {
    static final String defaultServiceName = "Node";
    String id;
    final String hostname;
    final int port;
    final String serviceName;
    IRemoteNode node;

    RemoteNode(String hostname, int port, String name) {
        this.hostname = hostname;
        this.port = port;
        this.serviceName = name;
    }


    RemoteNode(String hostname, int port) {
        this(hostname, port, RemoteNode.defaultServiceName);
    }

    IRemoteNode get() {
        return node;
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

//    boolean onConnect() {
//        try {
//            return this.node.declare(Settings.getSecret(), id, Settings.getRemoteHostname(), Settings.getRemotePort(), true);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

}
