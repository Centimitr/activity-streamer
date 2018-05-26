import com.google.gson.Gson;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
class NodesManager {
    private static String getId(String hostname, int port) {
        return hostname + ":" + port;
    }

    static final Gson g = new Gson();

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

    RemoteNode getFreeNode() {
        int leastLoad = local().getLoad() - 2;
        RemoteNode leastLoadNode = null;
        for (Map.Entry<String, RemoteNode> entry : remoteNodes.entrySet()) {
            RemoteNode node = entry.getValue();
            int load = node.getLoad();
            if (load <= leastLoad) {
                leastLoad = load;
                leastLoadNode = node;
            }
        }
        return leastLoadNode;
    }

    RemoteNode getBusyNode() {
        int mostLoad = local().getLoad();
        RemoteNode mostLoadNode = null;
        for (Map.Entry<String, RemoteNode> entry : remoteNodes.entrySet()) {
            RemoteNode node = entry.getValue();
            int load = node.getLoad();
            if (load >= mostLoad) {
                mostLoad = load;
                mostLoadNode = node;
            }
        }
        return mostLoadNode;
    }

    ArrayList<IRemoteNode> nodes() {
        ArrayList<IRemoteNode> nodes = new ArrayList<>();
        nodes.add(local().get());
        remoteNodes.values().forEach(node -> nodes.add(node.get()));
        return nodes;
    }

    // group methods
    void sendMessages(String sender, ArrayList<String> receivers, MsgActivityBroadcast msg, boolean canSpread) {
        // retry until SESSION_TIMEOUT
        for (IRemoteNode node : nodes()) {
            try {
                node.sendMessage(sender, receivers, msg, canSpread);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    boolean register(String id, String username, String secret) {
        // retry until SESSION_TIMEOUT
        for (IRemoteNode node : nodes()) {
            try {
                node.register(id, username, secret);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    void updateLoads() {
        // run frequently so when meeting exceptions, does need to retry
        for (Map.Entry<String, RemoteNode> entry : remoteNodes.entrySet()) {
            RemoteNode local = entry.getValue();
            IRemoteNode remote = entry.getValue().node;
            try {
                local.updateLoad(remote.getLoad());
            } catch (RemoteException ignored) {
            }
        }
    }

}
