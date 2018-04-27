import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class ClientAgent extends ConnectivityAgent {
    private static Gson g = new Gson();
    private boolean needReconnect = false;
    String reconnectHostname;
    int reconnectPort;
    Lock registerLock = new Lock();
    Lock loginLock = new Lock();
    boolean viewConnectLock = true;
    WebEngine engine;
    ArrayList<String> scripts = new ArrayList<>();

    void bindEngine(WebEngine engine) {
        this.engine = engine;
        viewConnectLock = false;
        scripts.forEach(this::exec);
        scripts.clear();
    }

    private void exec(String script) {
        System.out.println("Exec: " + script);
        Platform.runLater(() -> this.engine.executeScript("(window['devbycmstream'])." + script));
    }

    void eval(String script) {
        if (this.viewConnectLock) {
            this.scripts.add(script);
            return;
        }
        exec(script);
    }

    /*
        UI Methods
     */

    void uiSetLoaded(boolean needRegister) {
        eval(String.format("setLoaded(%s, '%s', '%s')",
                needRegister,
                Settings.getUsername(),
                Settings.getSecret()
        ));
    }

    void uiSetRegistered(boolean success) {
        eval(String.format("setRegistered(%s, '%s', '%s')",
                success,
                Settings.getUsername(),
                Settings.getSecret()
        ));
    }

    void uiSetLoggedIn(boolean success) {
        eval(String.format("setLoggedIn(%s, '%s', '%s')", success, Settings.getRemoteHostname(), Settings.getRemotePort()));
    }

    void uiAddMessage(Map<Object, Object> activity) {
        eval(String.format("addMessage('%s')", g.toJson(activity)));
    }

    /*
        Reconnect
     */

    void setReconnectDetail(String hostname, int port) {
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

    public void logout() {
        MsgLogout m = new MsgLogout();
        sendln(m);
        close();
    }

    void sendActivityObject(JsonObject obj) {
        Type type = new TypeToken<Map<Object, Object>>() {
        }.getType();
        MsgActivityMessage m = new MsgActivityMessage(
                Settings.getUsername(),
                Settings.getSecret(),
                g.fromJson(obj, type)
        );
        sendln(m);
    }

    public void send(String message) {
        System.out.println("send" + message);
        Type type = new TypeToken<Map<Object, Object>>() {
        }.getType();
        MsgActivityMessage m = new MsgActivityMessage(
                Settings.getUsername(),
                Settings.getSecret(),
                g.fromJson(message, type)
        );
        sendln(m);
    }
}
