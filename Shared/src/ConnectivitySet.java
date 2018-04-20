import java.util.ArrayList;
import java.util.function.Consumer;

interface ImmutableConnectivitySet {
    void forEach(Consumer<Connectivity> fn);

    void broadcast(Object obj);
}

interface MutableConnectivitySet extends ImmutableConnectivitySet {
    boolean add(Connectivity c);

    boolean remove(Connectivity c);

    ConnectivitySet exclude(Connectivity toExclude);

    ConnectivitySet exclude(MessageContext toExclude);

    void transfer(Connectivity c, MutableConnectivitySet target);
}

class ConnectivitySet implements MutableConnectivitySet {
    private ArrayList<Connectivity> conns = new ArrayList<>();

    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public boolean add(Connectivity c) {
        return conns.add(c);
    }

    public boolean remove(Connectivity c) {
        return conns.remove(c);
    }

//    ArrayList<Connectivity> get() {
//        return conns;
//    }

    public void forEach(Consumer<Connectivity> fn) {
        conns.forEach(fn);
    }

    public void broadcast(Object obj) {
        conns.forEach(conn -> conn.sendln(obj));
    }

//    ConnectivitySet concat(ConnectivitySet src) {
//        ConnectivitySet set = new ConnectivitySet();
//        conns.forEach(set::add);
//        src.conns.forEach(src::add);
//        return set;
//    }

    @SuppressWarnings("WeakerAccess")
    public ConnectivitySet exclude(Connectivity toExclude) {
        if (toExclude != null) {
            ConnectivitySet set = new ConnectivitySet();
            conns.forEach(conn -> {
                if (!conn.equals(toExclude)) {
                    set.add(conn);
                }
            });
            return set;
        }
        return this;
    }

    public ConnectivitySet exclude(MessageContext toExclude) {
        if (toExclude != null) {
            return exclude(toExclude.connectivity);
        }
        return this;
    }

    public void transfer(Connectivity c, MutableConnectivitySet target) {
        remove(c);
        target.add(c);
    }
}

class SingleConnectivitySet extends ConnectivitySet {
}


class ConnectivitySetGroup implements ImmutableConnectivitySet {
    private ArrayList<ConnectivitySet> group = new ArrayList<>();

    ConnectivitySetGroup(ConnectivitySet a, ConnectivitySet b) {
        group.add(a);
        group.add(b);
    }

    ConnectivitySetGroup(ConnectivitySet a, ConnectivitySet b, ConnectivitySet c) {
        this(a, b);
        group.add(c);
    }

    public void forEach(Consumer<Connectivity> fn) {
        group.forEach(set -> set.forEach(fn));
    }

    public void broadcast(Object obj) {
        group.forEach(set -> set.broadcast(obj));
    }
}