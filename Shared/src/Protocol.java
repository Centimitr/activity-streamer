import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/*
 * Message Enum
 */

enum MessageCommands {
    //    LOGOUT("LOGOUT"),
//    // info
//    INVALID_MESSAGE("INVALID_MESSAGE"),
//    AUTHTENTICATION_FAIL("AUTHTENTICATION_FAIL"),
//    LOGIN_SUCCESS("LOGIN_SUCCESS"),
//    LOGIN_FAILED("LOGIN_FAILED"),
//    REGISTER_FAILED("REGISTER_FAILED"),
//    REGISTER_SUCCESS("REGISTER_SUCCESS"),
//    // secret
//    AUTHENTICATE("AUTHENTICATE"),
//    // secret, username
//    LOGIN("LOGIN"),
//    REGISTER("REGISTER"),
//    LOCK_REQUEST("LOCK_REQUEST"),
//    LOCK_DENIED("LOCK_DENIED"),
//    LOCK_ALLOWED("LOCK_ALLOWED"),
//    // secret, username, activity
//    ACTIVITY_MESSAGE("ACTIVITY_MESSAGE"),
//    // activity
//    ACTIVITY_BROADCAST("ACTIVITY_BROADCAST"),
//    // hostname, port
//    REDIRECT("REDIRECT"),
//    // hostname, port, id, load
//    SERVER_ANNOUNCE("SERVER_ANNOUNCE");

    LOGOUT,
    // info
    INVALID_MESSAGE,
    AUTHTENTICATION_FAIL,
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    REGISTER_FAILED,
    REGISTER_SUCCESS,
    // secret
    AUTHENTICATE,
    // secret, username
    LOGIN,
    REGISTER,
    LOCK_REQUEST,
    LOCK_DENIED,
    LOCK_ALLOWED,
    // secret, username, activity
    ACTIVITY_MESSAGE,
    // activity
    ACTIVITY_BROADCAST,
    // hostname, port
    REDIRECT,
    // hostname, port, id, load
    SERVER_ANNOUNCE;

    public static boolean contains(String command) {
        for (MessageCommands c : MessageCommands.values()) {
            if (c.name().equals(command)) {
                return true;
            }
        }
        return false;
    }
}

/*
 * Message Protocol
 */

class MessageProtocol {
    private static MessageProtocol ourInstance = new MessageProtocol();
    private static final Logger log = LogManager.getLogger();

    //    private Map<String, Class> map = Map.ofEntries(
//            Map.entry("LOGOUT", Message.class),
//            // info
//            Map.entry("INVALID_MESSAGE", MessageInfo.class),
//            Map.entry("AUTHTENTICATION_FAIL", MessageInfo.class),
//            Map.entry("LOGIN_SUCCESS", MessageInfo.class),
//            Map.entry("LOGIN_FAILED", MessageInfo.class),
//            Map.entry("REGISTER_FAILED", MessageInfo.class),
//            Map.entry("REGISTER_SUCCESS", MessageInfo.class),
//            // secret
//            Map.entry("AUTHENTICATE", MessageSecret.class),
//            // secret, username
//            Map.entry("LOGIN", MessageUser.class),
//            Map.entry("REGISTER", MessageUser.class),
//            Map.entry("LOCK_REQUEST", MessageUser.class),
//            Map.entry("LOCK_DENIED", MessageUser.class),
//            Map.entry("LOCK_ALLOWED", MessageUser.class),
//            // secret, username, activity
//            Map.entry("ACTIVITY_MESSAGE", MessageActivity.class),
//            // activity
//            Map.entry("ACTIVITY_BROADCAST", MessageActivityBroadcast.class),
//            // hostname, port
//            Map.entry("REDIRECT", MessageServer.class),
//            // hostname, port, id, load
//            Map.entry("SERVER_ANNOUNCE", MessageServerAnnounce.class)
//    );
    public static MessageProtocol getInstance() {
        return ourInstance;
    }

    private MessageProtocol() {
    }

    private Map<String, Consumer<MessageContext>> handlers = new HashMap<>();

    boolean supportCommand(String command) {
        return MessageCommands.contains(command);
    }

    MessageProtocol registerHandler(MessageCommands command, Consumer<MessageContext> handler) {
        handlers.put(command.name(), handler);
        return this;
    }

    Consumer<MessageContext> getHandler(String command) {
        if (!supportCommand(command)) {
            log.warn("Protocol does not support command: " + command);
        }
        return handlers.get(command);
    }

}

/*
 * Message Context: Parse, Message, Mark
 */


class MessageContext {
    //    private static final Logger log = LogManager.getLogger();
    private static final Gson g = new Gson();
    public String command;
    private JsonObject j;
    private boolean willClose = false;

    public boolean is(String cmd) {
        return command.equals(cmd);
    }

    public boolean parse(String msg) {
        j = new JsonParser().parse(msg).getAsJsonObject();
        command = j.get("command").getAsString();
        return MessageProtocol.getInstance().supportCommand(command);
    }

    public <T> T read(Class<T> classOfT) {
        return g.fromJson(j, classOfT);
    }

    public void process() {
        Consumer<MessageContext> handler = MessageProtocol.getInstance().getHandler(command);
        handler.accept(this);
    }

    public void close() {
        willClose = true;
    }

    public boolean needClose() {
        return willClose;
    }

}


/*
 * Message Structures
 */

class Message {
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