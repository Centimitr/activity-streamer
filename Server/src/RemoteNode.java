import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

@SuppressWarnings("WeakerAccess")
class RemoteNode extends Node {
    String id;
    final String hostname;
    final int port;
    final String clientHostname;
    final int clientPort;
    final String serviceName;

    RemoteNode(String hostname, int port, String clientHostname, int clientPort, String name) {
        this.hostname = hostname;
        this.port = port;
        this.serviceName = name;
        this.clientHostname = clientHostname;
        this.clientPort = clientPort;
    }

    RemoteNode(String hostname, int port, String clientHostname, int clientPort) {
        this(hostname, port, clientHostname, clientPort, RemoteNode.defaultServiceName);
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
