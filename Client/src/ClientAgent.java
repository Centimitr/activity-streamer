import com.google.gson.Gson;
import com.google.gson.JsonObject;

@SuppressWarnings("WeakerAccess")
class ClientAgent extends Agent {
    private static Gson g = new Gson();
    private boolean needReconnect = false;
    String reconnectHostname;
    int reconnectPort;
    Lock registerLock = new Lock();
    Lock loginLock = new Lock();


    void reconnect(String hostname, int port) {
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

    String register(String username) {
        String secret = Settings.nextSecret();
        MsgRegister m = new MsgRegister(username, secret);
        sendln(m);
        registerLock.lock();
        return secret;
    }


    void login(String username, String secret) {
        MsgLogin m = new MsgLogin(username, secret);
        sendln(m);
        loginLock.lock();
    }

    void logout() {
        MsgLogout m = new MsgLogout();
        sendln(m);
        close();
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
