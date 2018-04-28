import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface IMessageRouter {

    boolean support(String command);

    Consumer<MessageContext> getHandler(Connectivity conn, String command);

    BiConsumer<MessageContext,String> getErrorHandler(Connectivity conn);
}
