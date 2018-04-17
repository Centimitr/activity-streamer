import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.function.Consumer;

@SuppressWarnings("WeakerAccess")
class MessageContext {
    private static final Gson g = new Gson();
    private JsonObject j;
    public String command;
    private boolean willClose;
    private MessageRouter router;
    private String reply;
    private String lastCommand;

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
        boolean valid = parse(msg);
        handle(valid);
        if (reply != null) {
            c.sendln(reply);
        }
        // todo: remove this debug print
        c.sendln("RJ: " + msg);
        return needClose();
    }

    public void handle(boolean valid) {
        Consumer<MessageContext> handler = valid ?
                router.getHandler(command) :
                router.getErrorHandler();
        handler.accept(this);
        lastCommand = command;
    }

    public boolean needClose() {
        return willClose;
    }

    /*
     * Handler Methods
     */

    public boolean after(String cmd) {
        return lastCommand.equals(cmd);
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
