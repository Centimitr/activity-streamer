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
        MessageUser m = new MessageUser(MessageCommands.REGISTER.name(),
                secret,
                username);
        sendln(m);
    }

    void login(String username, String secret) {
        MessageUser m = new MessageUser(MessageCommands.LOGIN.name(),
                secret,
                username);
        sendln(m);
    }

    void sendActivity(Object obj) {
        MessageActivity m = new MessageActivity(
                MessageCommands.ACTIVITY_MESSAGE.name(),
                Settings.getSecret(),
                Settings.getUsername(),
                obj
        );
        sendln(m);
    }

    void sendActivityObject(JsonObject obj) {
        // todo: may fail when cast types
        sendActivity(obj);
    }
}
