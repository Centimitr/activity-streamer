import java.util.ArrayList;
import java.util.function.Consumer;

class ConnectivitySet extends MessageRouterRepresenter implements IForEachConnectivity {

    private IMessageRouter realRouter;

    void bindRouter(IMessageRouter router) {
        this.realRouter = router;
    }

    static ConnectivitySet exclude(IForEachConnectivity setOrGroup, Connectivity toExclude) {
        ConnectivitySet set = new ConnectivitySet();
        setOrGroup.forEach(conn -> {
            if (!conn.equals(toExclude)) {
                set.add(conn);
            }
        });
        return set;
    }

    private ArrayList<Connectivity> conns = new ArrayList<>();

    Integer size() {
        return conns.size();
    }

    public synchronized boolean add(Connectivity c) {
        c.bindRouter(realRouter);
        c.whenClosed(() -> remove(c));
        return conns.add(c);
    }

    boolean contains(Connectivity c) {
        return conns.contains(c);
    }

    @SuppressWarnings("WeakerAccess")
    synchronized void remove(Connectivity c) {
        conns.remove(c);
    }

    synchronized void closeAll() {
        forEach(Connectivity::close);
        // possible meaningless, reference ConnectivitySet::add(Connectivity c)
        conns.clear();
    }

    public void forEach(Consumer<Connectivity> fn) {
        conns.forEach(fn);
    }

    void broadcast(Object obj) {
        conns.forEach(conn -> conn.sendln(obj));
    }

    public ConnectivitySet exclude(Connectivity toExclude) {
        return ConnectivitySet.exclude(this, toExclude);
    }

    synchronized void transfer(Connectivity c, ConnectivitySet target) {
        // todo: route bind
        remove(c);
        target.add(c);
    }
}
