import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

class MessageRouter implements IMessageRouter {
    private static final Logger log = LogManager.getLogger();

    MessageRouter() {
    }

    private Map<String, Consumer<MessageContext>> handlers = new HashMap<>();
    private BiConsumer<MessageContext,String> errorHandler = (c,error) -> {
        log.warn("No unknown handler configured but invoked.");
    };
    private Consumer<MessageContext> parseHandler = c -> {
        log.warn("Json Parse Error.");
    };

    @Override
    public boolean support(String command) {
        for (Map.Entry<String, Consumer<MessageContext>> handler : handlers.entrySet()) {
            if (handler.getKey().equals(command)) {
                return true;
            }
        }
        return false;
    }

    MessageRouter handle(MessageCommands command, Consumer<MessageContext> handler) {
        handlers.put(command.name(), handler);
        return this;
    }

    void handleError(BiConsumer<MessageContext,String> handler) {
        errorHandler = handler;
    }

    void handleParseError(Consumer<MessageContext> handler) { parseHandler = handler; }

    @Override
    public Consumer<MessageContext> getHandler(Connectivity conn, String command) {
        if (!support(command)) {
            log.warn("Protocol does not support command: " + command);
        }
        return handlers.get(command);
    }

    @Override
    public BiConsumer<MessageContext,String> getErrorHandler(Connectivity conn) {
        if (errorHandler == null) {
            log.error("No error handler is set, error will be dismissed.");
        }
        return errorHandler;
    }

}
