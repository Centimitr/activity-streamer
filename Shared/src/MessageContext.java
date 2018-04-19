import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("WeakerAccess")
class MessageContext {
    private static final Logger log = LogManager.getLogger();
    private static final Gson g = new Gson();
    private JsonObject j;
    public String command;
    public String lastCommand;
    public Connectivity connectivity;
    private boolean willClose;
    private MessageRouter router;
    private String reply;
    private Map<String, String> states = new HashMap<>();


    MessageContext(MessageRouter router) {
        this.router = router;
    }

    public boolean is(String cmd) {
        return command.equals(cmd);
    }

    public boolean parse(String msg) {
        j = new JsonParser().parse(msg).getAsJsonObject();
        command = j.get("command").getAsString();
        willClose = false;
        reply = null;
        return router.supportCommand(command);
    }

    public boolean process(Connectivity c, String msg) {
        System.out.println("Msg: " + msg);
        boolean valid = parse(msg);
        connectivity = c;
//        System.out.println("Valid: " + valid);
        handle(valid);
        if (reply != null) {
            c.sendln(reply);
        }
        reply = null;
        // todo: remove this debug print
//        c.sendln("RJ: " + msg);
        return needClose();
    }

    public void handle(boolean valid) {
        Consumer<MessageContext> handler = valid ?
                router.getHandler(command) :
                router.getErrorHandler();
        if (handler == null) {
            log.warn("No handler for message:" + g.toJson(j));
            return;
        }
        handler.accept(this);
        lastCommand = command;
    }

    public boolean needClose() {
        return willClose;
    }

    /*
     * Handler Methods
     */

    public String get(String key) {
        return states.get(key);
    }

    public String set(String key, String value) {
        return states.put(key, value);
    }

    public MessageContext after(String cmd, Consumer<String> callback) {
        if (lastCommand.equals(cmd)) {
            callback.accept(lastCommand);
        }
        return this;
    }

    public JsonObject read() {
        return j;
    }

    public <T> T read(Class<T> classOfT) {
        return g.fromJson(j, classOfT);
    }

    public void write(String reply) {
        this.reply = reply;
    }

    public void write(Object obj) {
        write(g.toJson(obj));
    }

    public void close() {
        willClose = true;
    }
}
