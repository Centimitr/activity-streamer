import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

class MessageRouter {
    private static final Logger log = LogManager.getLogger();

    MessageRouter() {
    }

    private Map<String, Consumer<MessageContext>> handlers = new HashMap<>();
    private Consumer<MessageContext> errorHandler = c -> {
        log.warn("No unknown handler configured but invoked.");
    };

    boolean supportCommand(String command) {
        return MessageCommands.contains(command);
    }

    MessageRouter registerHandler(MessageCommands command, Consumer<MessageContext> handler) {
        handlers.put(command.name(), handler);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    MessageRouter registerErrorHandler(Consumer<MessageContext> handler) {
        errorHandler = handler;
        return this;
    }

    Consumer<MessageContext> getHandler(String command) {
        if (!supportCommand(command)) {
            log.warn("Protocol does not support command: " + command);
        }
        return handlers.get(command);
    }

    Consumer<MessageContext> getErrorHandler() {
        if (errorHandler == null) {
            log.error("No error handler is set, error will be dismissed.");
        }
        return errorHandler;
    }

}
