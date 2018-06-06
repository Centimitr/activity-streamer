import com.google.gson.Gson;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@SuppressWarnings("WeakerAccess")
class NodesManager {
    private static String getId(String hostname, int port) {
        return hostname + ":" + port;
    }

    static final Gson g = new Gson();

    private final LocalNode localNode = new LocalNode();
    private ConcurrentHashMap<String, RemoteNode> remoteNodes = new ConcurrentHashMap<>();

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

    void put(String hostname, int port, String clientHostname, int clientPort) {
        RemoteNode node = get(hostname, port);
        if (node != null) {
            node.clientHostname = clientHostname;
            node.clientPort = clientPort;
        }
    }

    RemoteNode add(String hostname, int port, String clientHostname, int clientPort) {
        if (has(hostname, port)) {
            return null;
        }
        RemoteNode node = new RemoteNode(hostname, port, clientHostname, clientPort);
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
    void sendMessages(String sender, int index, String msg) {
        ArrayList<String> allFailedUsers = new ArrayList<>();
        for (IRemoteNode node : nodes()) {
            Supplier<Boolean> fn = () -> {
                try {
                    ArrayList<String> receivers = node.getUserList();
                    ArrayList<String> failedUsers = node.sendMessage(sender, index, receivers, msg, false);
                    allFailedUsers.addAll(failedUsers);
                    return true;
                } catch (RemoteException e) {
                    return false;
                }
            };
            Util.retry(fn, Env.RETRY_INTERVAL, Env.INTERNAL_NETWORK_TIMEOUT);
        }
        for (IRemoteNode node : nodes()) {
            Supplier<Boolean> fn = () -> {
                try {
                    node.sendMessage(sender, index, allFailedUsers, msg, true);
                    return true;
                } catch (RemoteException e) {
                    return false;
                }
            };
            Util.retry(fn, Env.RETRY_INTERVAL, Env.INTERNAL_NETWORK_TIMEOUT);
        }
    }

    boolean register(String id, String username, String secret) {
        // retry until SESSION_TIMEOUT
        for (IRemoteNode node : nodes()) {
            Supplier<Boolean> fn = () -> {
                try {
                    node.register(id, username, secret);
                    return true;
                } catch (RemoteException e) {
                    return false;
                }
            };
            Util.retry(fn, Env.RETRY_INTERVAL, Env.INTERNAL_NETWORK_TIMEOUT);
        }
        return true;
    }

    void updateLoads() {
        // updateLoadNotify frequently so when meeting exceptions, does not need to retry
        for (Map.Entry<String, RemoteNode> entry : remoteNodes.entrySet()) {
            RemoteNode local = entry.getValue();
            IRemoteNode remote = entry.getValue().get();
//            Supplier<Boolean> fn = () -> {
            try {
                local.updateLoad(remote.getLoad());
//                    return true;
            } catch (RemoteException e) {
//                    return false;
            }
//            };
//            Util.retry(fn, Env.RETRY_INTERVAL, Env.INTERNAL_NETWORK_TIMEOUT);
        }
    }

    // recover
    public ArrayList<EndPoint> snapshot() {
        ArrayList<EndPoint> nodesToConnect = new ArrayList<>();
        for (Map.Entry<String, RemoteNode> entry : remoteNodes.entrySet()) {
            RemoteNode node = entry.getValue();
            EndPoint endpoint = new EndPoint(node.hostname, node.port);
            nodesToConnect.add(endpoint);
        }
        return nodesToConnect;
    }
}
