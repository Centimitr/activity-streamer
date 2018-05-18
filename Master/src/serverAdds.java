import java.util.HashMap;
import java.util.Map;

class serverAdd {
    String hostname;
    int port;

    serverAdd(String hostname, int port){
        this.hostname = hostname;
        this.port = port;
    }
}

class serverAdds {
    private Map<String, serverAdd> addresses = new HashMap<>();

    void connectAll(){

    }

    boolean has(String hostname, int port){
        String id = hostname + "_" + port;
        return (addresses.containsKey(id));
    }

    void update(String hostname, int port){
        String id = hostname + "_" + port;
        serverAdd server = new serverAdd(hostname,port);
        addresses.put(id, server);
    }
}
