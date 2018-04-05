import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

class Protocol {
    static Map<String, Class<? extends Message>> map = Map.ofEntries(
            Map.entry("LOGOUT", Message.class),
            // info
            Map.entry("INVALID_MESSAGE", MessageInfo.class),
            Map.entry("AUTHTENTICATION_FAIL", MessageInfo.class),
            Map.entry("LOGIN_SUCCESS", MessageInfo.class),
            Map.entry("LOGIN_FAILED", MessageInfo.class),
            Map.entry("REGISTER_FAILED", MessageInfo.class),
            Map.entry("REGISTER_SUCCESS", MessageInfo.class),
            // secret
            Map.entry("AUTHENTICATE", MessageSecret.class),
            // secret, username
            Map.entry("LOGIN", MessageUser.class),
            Map.entry("REGISTER", MessageUser.class),
            Map.entry("LOCK_REQUEST", MessageUser.class),
            Map.entry("LOCK_DENIED", MessageUser.class),
            Map.entry("LOCK_ALLOWED", MessageUser.class),
            // secret, username, activity
            Map.entry("ACTIVITY_MESSAGE", MessageActivity.class),
            // activity
            Map.entry("ACTIVITY_BROADCAST", MessageActivityBroadcast.class),
            // hostname, port
            Map.entry("REDIRECT", MessageServer.class),
            // hostname, port, id, load
            Map.entry("SERVER_ANNOUNCE", MessageServerAnnounce.class)
    );
}

class MessageParser {
    private static final Logger log = LogManager.getLogger();
    private static final Gson g = new Gson();
    public String command;
    public MessageInterface message;

    public boolean is(String cmd) {
        return command.equals(cmd);
    }

    public boolean parse(String msg) {
        JsonObject j = new JsonParser().parse(msg).getAsJsonObject();
        command = j.get("command").getAsString();
        Class<? extends Message> c = Protocol.map.get(command);
        message = g.fromJson(j, c);
        return true;
    }
}

interface MessageInterface {

}


class Message implements MessageInterface {
    String command;
}

class MessageSecret extends Message {
    String secret;
}

class MessageInfo extends Message {
    String info;
}

class MessageUser extends MessageSecret {
    String username;
}

class MessageActivity extends MessageUser {
    String activity;
}

class MessageServer extends Message {
    String hostname;
    String port;
}

class MessageServerAnnounce extends MessageServer {
    String id;
    Integer load;
}

class MessageActivityBroadcast extends Message {
    String activity;
}