import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

class MessageProtocol {
    private static MessageProtocol ourInstance = new MessageProtocol();
    private static final Logger log = LogManager.getLogger();

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
