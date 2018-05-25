//import org.w3c.dom.Node;
//
//import java.util.*;
//import java.util.function.BiConsumer;
//import java.util.function.Function;
//
//@SuppressWarnings("WeakerAccess")
//class ServerRecord {
//    String id;
//    String hostname;
//    Integer port;
//    Integer load;
//
//    ServerRecord(String id, String hostname, Integer port, Integer load) {
//        this.id = id;
//        update(hostname, port, load);
//    }
//
//    void update(String hostname, Integer port, Integer load) {
//        this.hostname = hostname;
//        this.port = port;
//        this.load = load;
//    }
//}
//
//@SuppressWarnings({"WeakerAccess", "SameParameterValue"})
//class ServerRecordSet {
//    private Map<String, ServerRecord> records = new HashMap<>();
//
//    boolean has(String id) {
//        return records.containsKey(id);
//    }
//
//    ServerRecord get(String id) {
//        return records.get(id);
//    }
//
//    void put(String id, String hostname, int port, int load) {
//        if (has(id)) {
//            ServerRecord r = get(id);
//            r.update(hostname, port, load);
//        } else {
//            ServerRecord r = new ServerRecord(id, hostname, port, load);
//            records.put(id, r);
//        }
//    }
//
//    void forEach(BiConsumer<String, ServerRecord> fn) {
//        records.forEach(fn);
//    }
//
//    ServerRecord getLoadLowerThan(int load) {
//        for (Map.Entry<String, ServerRecord> entry : records.entrySet()) {
//            ServerRecord record = entry.getValue();
//            if (record.load <= load) {
//                return record;
//            }
//        }
//        return null;
//    }
//
//    ServerRecord getLoadLeast(int load) {
//        ServerRecord r = null;
//        for (Map.Entry<String, ServerRecord> entry : records.entrySet()) {
//            ServerRecord record = entry.getValue();
//            if (r == null || record.load < r.load) {
//                r = record;
//            }
//        }
//        if (r != null && r.load <= load) {
//            return r;
//        }
//        return null;
//    }
//}
//
//@SuppressWarnings("WeakerAccess")
//class ServiceBalancer {
//
//    public static Function<Integer, Integer> CALC_AVAILABLE_LOAD = load -> (load - 2);
//    public static int CHOSEN_RULE = 0;
//
//    private ServerRecordSet set;
//
//    ServiceBalancer(ServerRecordSet set) {
//        this.set = set;
//    }
//
//    ServerRecord getAvailableServer(int load) {
//        int targetLoad = CALC_AVAILABLE_LOAD.apply(load);
//        ServerRecord record;
//        switch (CHOSEN_RULE) {
//            case 0:
//                record = set.getLoadLowerThan(targetLoad);
//                break;
//            case 1:
//            default:
//                record = set.getLoadLeast(targetLoad);
//        }
//        return record;
//    }
//}
//
//class Servers implements IRecoverable {
//    private ServerRecordSet records = new ServerRecordSet();
//    private ServiceBalancer balancer = new ServiceBalancer(records);
//    private NodesManager nm;
//
//    Servers(NodesManager nm) {
//        this.nm = nm;
//    }
//
//    ServiceBalancer balancer() {
//        return balancer;
//    }
//
//    ServerRecordSet records() {
//        return records;
//    }
//
////    int num() {
//////        HashMap<String, Boolean> addresses = new HashMap<>();
//////        records.forEach((id, record) -> {
//////            String address = record.hostname + ":" + record.port;
//////            addresses.put(address, true);
//////        });
//////        nm.forEach(node -> {
//////            // todo: might not be the correct address
//////            String ip = conn.socket().getInetAddress().getHostAddress();
//////            int port = conn.socket().getPort();
//////            String address = ip + ":" + port;
//////            addresses.put(address, true);
//////        });
//////        return addresses.size();
//////    }
//
//    @Override
//    public String snapshot() {
//        return "";
//    }
//
//    @Override
//    public void recover(String snapshot) {
//
//    }
//
//}