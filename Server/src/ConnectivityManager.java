class ConnectivityManager {
    private ConnectivitySet temp = new ConnectivitySet();
    private ConnectivitySet servers = new ConnectivitySet();
    private ConnectivitySet clients = new ConnectivitySet();
    private SingleConnectivitySet server = new SingleConnectivitySet();

    private ConnectivitySetGroup allServers = new ConnectivitySetGroup(servers, server);
    private ConnectivitySetGroup all = new ConnectivitySetGroup(servers, clients, server);

    MutableConnectivitySet temp() {
        return temp;
    }

    MutableConnectivitySet clients() {
        return clients;
    }

    MutableConnectivitySet parent() {
        return server;
    }

    MutableConnectivitySet children() {
        return servers;
    }

    ImmutableConnectivitySet servers() {
        return allServers;
    }

    ImmutableConnectivitySet all() {
        return all;
    }
}
