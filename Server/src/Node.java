import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

abstract class Node {
    static final String defaultServiceName = "Node";
    static final Logger log = LogManager.getLogger();
    IRemoteNode node;
    private int load = 0;

    IRemoteNode get() {
        return node;
    }

    void updateLoad(int load) {
        this.load = load;
    }

    int getLoad() {
        return load;
    }
}
