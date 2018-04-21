import java.util.*;
import java.util.function.BiConsumer;

class ServerRecord {
    String id;
    String hostname;
    Integer port;
    Integer load;

    ServerRecord(String id, String hostname, Integer port, Integer load) {
        this.id = id;
        update(hostname, port, load);
    }

    void update(String hostname, Integer port, Integer load) {
        this.hostname = hostname;
        this.port = port;
        this.load = load;
    }
}

class ServerRecordSet {
    private Map<String, ServerRecord> records = new HashMap<>();

    boolean has(String id) {
        return records.containsKey(id);
    }

    ServerRecord get(String id) {
        return records.get(id);
    }

    void put(String id, String hostname, int port, int load) {
        if (has(id)) {
            ServerRecord r = get(id);
            r.update(hostname, port, load);
        } else {
            ServerRecord r = new ServerRecord(id, hostname, port, load);
            records.put(id, r);
        }
    }

    void forEach(BiConsumer<String, ServerRecord> fn) {
        records.forEach(fn);
    }
}


class ServiceBalancer {
    static int calcAvailableLoad(int load) {
        return load - 2;
    }

    private ServerRecordSet set;
    public int rule = 0;

    ServiceBalancer(ServerRecordSet set) {
        this.set = set;
    }


    ArrayList<ServerRecord> recordsWithLoadLowerThan(int num) {

    }

    ServerRecord recordWithLoadLowerThan(int num) {

    }

    ServerRecord recordWithLeastLoad(int num) {

    }

    ServerRecord available(int load) {
        int targetLoad = calcAvailableLoad(load);
        ServerRecord record;
        switch (rule) {
            case 0:
                record = recordWithLoadLowerThan(targetLoad);
                break;
            case 1:
            default:
                record = recordWithLeastLoad(targetLoad);
        }
        return record;
    }
}


public class Servers {
    private ConnectivitySetGroup connectedServers;
    private ServerRecordSet records = new ServerRecordSet();
    private ServiceBalancer balancer = new ServiceBalancer(records);

    Servers(ConnectivitySetGroup servers) {
        this.connectedServers = servers;
    }

    public ServiceBalancer balancer() {
        return balancer;
    }

    ServerRecordSet records() {
        return records;
    }

    int num() {
        HashMap<String, Boolean> addresses = new HashMap<>();
        records.forEach((id, record) -> {
            String address = record.hostname + ":" + record.port;
            addresses.put(address, true);
        });
        connectedServers.forEach(conn -> {
            // todo: might not be the correct address
            String ip = conn.socket().getInetAddress().getHostAddress();
            int port = conn.socket().getPort();
            String address = ip + ":" + port;
            addresses.put(address, true);
        });
        return addresses.size();
    }
}