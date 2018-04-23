import java.util.ArrayList;
import java.util.function.Consumer;

class ConnectivitySetGroup extends MessageRouterRepresenter implements IForEachConnectivity {

    private ArrayList<ConnectivitySet> group = new ArrayList<>();

    ConnectivitySetGroup(ConnectivitySet a, ConnectivitySet b) {
        group.add(a);
        group.add(b);
    }

    ConnectivitySetGroup(ConnectivitySet a, ConnectivitySet b, ConnectivitySet c) {
        this(a, b);
        group.add(c);
    }

    /*
     *  Router
     */

    //    private IMessageRouter router;
//
//    IMessageRouter router() {
//        return router;
//    }
//
//    void bindRouter(IMessageRouter router) {
////        this.router = router;
//        sets().forEach(set -> set.bindRouter(router));
//    }

    /*
     *  Sets
     */

    ArrayList<ConnectivitySet> sets() {
        return group;
    }

    boolean contains(Connectivity c) {
        return owner(c) != null;
    }

    public void forEach(Consumer<Connectivity> fn) {
        group.forEach(set -> set.forEach(fn));
    }

    ConnectivitySet exclude(Connectivity toExclude) {
        return ConnectivitySet.exclude(this, toExclude);
    }

    @SuppressWarnings("WeakerAccess")
    ConnectivitySet owner(Connectivity c) {
        for (ConnectivitySet set : group) {
            if (set.contains(c)) {
                return set;
            }
        }
        return null;
    }

    /*
     *  Method
     */

    void broadcast(Object obj) {
        group.forEach(set -> set.broadcast(obj));
    }

}
