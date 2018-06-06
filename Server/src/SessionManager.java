import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class SessionManager {
    private Map<String, Long> sessions = new HashMap<>();
    private Map<String, Connectivity> conns = new HashMap<>();

    void markAsOnline(String username, Connectivity conn) {
        long time = System.currentTimeMillis();
        sessions.put(username, time);
        conns.put(username, conn);
    }

    void markAsOffline(String username) {
        sessions.remove(username);
        conns.remove(username);
    }

    private void filter() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : sessions.entrySet()) {
            long lastActivityTime = entry.getValue();
            if (now - lastActivityTime > Env.SESSION_TIMEOUT) {
                markAsOffline(entry.getKey());
            }
        }
        for (Map.Entry<String, Connectivity> entry : conns.entrySet()) {
            if (entry.getValue().isClosed()) {
                markAsOffline(entry.getKey());
            }
        }
    }

    ArrayList<String> getUserList() {
        filter();
        return new ArrayList<>(sessions.keySet());
    }

    Map<String, Connectivity> getConnectivities() {
        filter();
        return conns;
    }
}
