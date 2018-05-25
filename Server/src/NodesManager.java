import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
class NodesManager {
    private static String getId(String hostname, int port) {
        return hostname + ":" + port;
    }

    private final LocalNode localNode = new LocalNode();
    private final Map<String, RemoteNode> remoteNodes = new HashMap<>();

    LocalNode local() {
        return localNode;
    }

    RemoteNode get(String hostname, int port) {
        String id = getId(hostname, port);
        return remoteNodes.get(id);
    }

    boolean has(String hostname, int port) {
        String id = getId(hostname, port);
        return remoteNodes.containsKey(id);
    }

    void put(String hostname, int port, RemoteNode node) {
        String id = getId(hostname, port);
        remoteNodes.put(id, node);
    }

    RemoteNode add(String hostname, int port) {
        if (has(hostname, port)) {
            return null;
        }
        RemoteNode node = new RemoteNode(hostname, port);
        boolean ok = node.connect();
        if (!ok) {
            return null;
        }
        put(hostname, port, node);
        return node;
    }

}
