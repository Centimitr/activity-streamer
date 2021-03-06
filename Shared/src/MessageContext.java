import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("WeakerAccess")
class MessageContext {
    private static final Logger log = LogManager.getLogger();
    private static final Gson g = new Gson();
    public Connectivity connectivity;
    private IMessageRouter router;
    private Map<String, String> states = new HashMap<>();
    // states
    public String command;
    public String lastCommand;
    private JsonObject j;
    private ArrayList<String> replies;
    private boolean willClose;

    MessageContext(IMessageRouter router) {
        if (router == null) {
            log.error("Router should not be null");
        }
        bindRouter(router);
    }

    void bindRouter(IMessageRouter router) {
        this.router = router;
    }

    public boolean is(String cmd) {
        return command.equals(cmd);
    }

    public boolean parse(String msg) {
        try {
            j = new JsonParser().parse(msg).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return false;
        }
        command = j.get("command").getAsString();
        return command != null;
    }

    private void clearState() {
        willClose = false;
        replies = new ArrayList<>();
    }

    private void handleStateChange() {
        lastCommand = command;
        replies.forEach(reply -> connectivity.sendln(reply));
    }

    public synchronized boolean process(Connectivity c, String msg) {
        if (c.isClosed()) {
            return true;
        }
        clearState();
        connectivity = c;
        boolean valid = parse(msg);
        handle(valid);
        handleStateChange();
        return willClose;
    }

    public void handle(boolean valid) {
        BiConsumer<MessageContext, String> errorHandler = router.getErrorHandler(connectivity);
        if (!valid) {
            errorHandler.accept(this, "Parse Error");
        }
        Consumer<MessageContext> handler = router.getHandler(connectivity, command);
        if (handler == null) {
            log.warn("No handler for message:" + g.toJson(j));
            errorHandler.accept(this, "No Command");
            return;
        }
        handler.accept(this);
    }

//    public boolean needClose() {
//        return willClose;
//    }

    /*
     * Handler Methods
     */

    public String get(String key) {
        return states.get(key);
    }

    public String set(String key, String value) {
        return states.put(key, value);
    }

    public void delete(String key) {
        states.remove(key);
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
        this.replies.add(reply);
    }

    public void write(Object obj) {
        write(g.toJson(obj));
    }

    public void close() {
        willClose = true;
    }
}
