import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

class ConnectivityManager {

    private final ConnectivitySet temp = new ConnectivitySet();
    private final ConnectivitySet clients = new ConnectivitySet();

    private final ConnectivitySetGroup possibleClients = new ConnectivitySetGroup(temp, clients);
    private final ConnectivitySetGroup all = new ConnectivitySetGroup(temp, clients);

    private final CentralRouter centralRouter = new CentralRouter(this);
    private final RouterManager routers = new RouterManager();

    ConnectivityManager() {

        all.sets().forEach(set -> set.bindRouter(centralRouter));

        // set represented routers
        temp.setRouter(routers.temp());
        clients.setRouter(routers.client());
        possibleClients.setRouter(routers.possibleClient());
    }

    RouterManager routerManager() {
        return routers;
    }

    ConnectivitySet temp() {
        return temp;
    }

    ConnectivitySet clients() {
        return clients;
    }

    ConnectivitySetGroup possibleClients() {
        return possibleClients;
    }

    ConnectivitySetGroup all() {
        return all;
    }
}

class CentralRouter implements IMessageRouter {
    static final Logger log = LogManager.getLogger();
    private ConnectivitySet[] primitives;
    private ConnectivitySetGroup[] compounds;

    CentralRouter(ConnectivityManager cm) {
        primitives = new ConnectivitySet[]{cm.temp(), cm.clients()};
        compounds = new ConnectivitySetGroup[]{cm.possibleClients(), cm.all()};
    }

    @Override
    public boolean support(String command) {
        return MessageCommands.contains(command);
    }

    @Override
    public Consumer<MessageContext> getHandler(Connectivity c, String command) {
        for (ConnectivitySet set : primitives) {
            if (set.contains(c)) {
//                log.info("Router support: "+ command+"? "+ set.router().support(command));
                if (set.router().support(command)) {
                    return set.router().getHandler(c, command);
                }
            }
        }
        for (ConnectivitySetGroup group : compounds) {
            if (group.contains(c)) {
                if (group.router() != null && group.router().support(command)) {
                    return group.router().getHandler(c, command);
                }
            }
        }
        return null;
    }

    @Override
    public BiConsumer<MessageContext, String> getErrorHandler(Connectivity c) {
        for (ConnectivitySet set : primitives) {
            if (set.contains(c)) {
                return set.router().getErrorHandler(c);
            }
        }
        return null;
    }

}

class RouterManager {
    private MessageRouter temp = new MessageRouter();
    private MessageRouter client = new MessageRouter();
    private MessageRouter possibleClient = new MessageRouter();

    MessageRouter temp() {
        return temp;
    }

    MessageRouter client() {
        return client;
    }

    MessageRouter possibleClient() {
        return possibleClient;
    }

}