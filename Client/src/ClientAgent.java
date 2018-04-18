import com.google.gson.Gson;
import org.json.simple.JSONObject;

class ClientAgent extends Agent {
    private static Gson g = new Gson();

    void sendActivityObject(JSONObject obj) {
        MessageActivity m = new MessageActivity(
                MessageCommands.ACTIVITY_MESSAGE.name(),
                Settings.getSecret(),
                Settings.getUsername(),
                g.toJson(obj)
        );
        sendln(m);
    }
}
