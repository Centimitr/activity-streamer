class ConnectivityManager {
    private static MessageRouter tempMessageRouter = new MessageRouter();
    private static MessageRouter clientMessageRouter = new MessageRouter();
    private static MessageRouter serverMessageRouter = new MessageRouter();

    private ConnectivitySet temp = new ConnectivitySet();
    private ConnectivitySet clients = new ConnectivitySet();
    private ConnectivitySet servers = new ConnectivitySet();
    private SingleConnectivitySet server = new SingleConnectivitySet();

    ConnectivityManager() {
        temp.bind(tempMessageRouter);
        clients.bind(clientMessageRouter);
        servers.bind(serverMessageRouter);
    }


    private ConnectivitySetGroup allServers = new ConnectivitySetGroup(servers, server);
    private ConnectivitySetGroup all = new ConnectivitySetGroup(servers, clients, server);

    ConnectivitySet temp() {
        return temp;
    }

    ConnectivitySet clients() {
        return clients;
    }

    SingleConnectivitySet parent() {
        return server;
    }

    ConnectivitySet children() {
        return servers;
    }

    ConnectivitySetGroup servers() {
        return allServers;
    }

    ConnectivitySetGroup all() {
        return all;
    }
}
