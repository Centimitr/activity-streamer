import java.util.function.Consumer;

public interface IMessageRouter {

    boolean support(String command);

    Consumer<MessageContext> getHandler(Connectivity conn, String command);

    Consumer<MessageContext> getErrorHandler(Connectivity conn);
}
