import com.google.gson.Gson;
import com.google.gson.JsonObject;

class ClientAgent extends Agent {
    private static Gson g = new Gson();
    private boolean needReconnect = false;
    String reconnectHostname;
    Integer reconnectPort;

    void reconnect(String hostname, Integer port) {
        needReconnect = true;
        reconnectHostname = hostname;
        reconnectPort = port;
    }

    boolean needReconnect() {
        if (needReconnect) {
            needReconnect = false;
            return true;
        }
        return false;
    }

    /*
        User Methods
     */

    void register(String username, String secret) {
        MsgRegister m = new MsgRegister(username, secret);
        sendln(m);
    }

    void login(String username, String secret) {
        MsgLogin m = new MsgLogin(username, secret);
        sendln(m);
    }

    void sendActivity(Object obj) {
        MsgActivityMessage m = new MsgActivityMessage(
                Settings.getUsername(),
                Settings.getSecret(),
                obj
        );
        sendln(m);
    }

    void sendActivityObject(JsonObject obj) {
        // todo: may fail when cast types
        sendActivity(obj);
    }
}
