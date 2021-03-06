import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

class RecoveryData {
    static final Gson g = new Gson();
    private HashMap<String, Account> registeredAccounts;
    private ArrayList<EndPoint> nodesToConnect;
    String clientHostname;
    int clientPort;

    RecoveryData(String clientHostname, int clientPort) {
        this.clientHostname = clientHostname;
        this.clientPort = clientPort;
    }

    void supply(RegisterManager rm, NodesManager nm) {
        registeredAccounts = rm.snapshot();
        nodesToConnect = nm.snapshot();
    }

    HashMap<String, Account> getRegisteredAccounts() {
        return registeredAccounts;
    }

    ArrayList<EndPoint> getNodesToConnect() {
        return nodesToConnect;
    }

    static RecoveryData fromJson(String json) {
        return g.fromJson(json, RecoveryData.class);
    }

    String toJson() {
        return g.toJson(this);
    }
}
