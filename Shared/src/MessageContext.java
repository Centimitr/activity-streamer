import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.function.Consumer;

@SuppressWarnings("WeakerAccess")
class MessageContext {
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
