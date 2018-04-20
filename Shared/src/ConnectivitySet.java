import java.util.ArrayList;
import java.util.function.Consumer;

interface IForEach {
    void forEach(Consumer<Connectivity> fn);
}
//interface ImmutableConnectivitySet {
//    void forEach(Consumer<Connectivity> fn);
//
//    void broadcast(Object obj);
//}
//
//interface MutableConnectivitySet extends ImmutableConnectivitySet {
//    boolean add(Connectivity c);
//
//    boolean remove(Connectivity c);
//
//    ConnectivitySet exclude(Connectivity toExclude);
//
//    ConnectivitySet exclude(MessageContext toExclude);
//
//    void transfer(Connectivity c, MutableConnectivitySet target);
//}
//
//interface MutableSingleConnectivitySet extends MutableConnectivitySet {
//
//}


class ConnectivitySet implements IForEach {
    //    class ConnectivitySet implements MutableConnectivitySet {
    static ConnectivitySet exclude(IForEach setOrGroup, Connectivity toExclude) {
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

    public boolean add(Connectivity c) {
        return conns.add(c);
    }

    boolean contains(Connectivity c) {
        return conns.contains(c);
    }

    void remove(Connectivity c) {
        conns.remove(c);
    }

    void closeAll() {
        forEach(conn -> {
            conn.close();
            remove(conn);
        });
    }

//    ArrayList<Connectivity> get() {
//        return conns;
//    }

    public void forEach(Consumer<Connectivity> fn) {
        conns.forEach(fn);
    }

    void broadcast(Object obj) {
        conns.forEach(conn -> conn.sendln(obj));
    }

//    ConnectivitySet concat(ConnectivitySet src) {
//        ConnectivitySet set = new ConnectivitySet();
//        conns.forEach(set::add);
//        src.conns.forEach(src::add);
//        return set;
//    }

    public ConnectivitySet exclude(Connectivity toExclude) {
        return ConnectivitySet.exclude(this, toExclude);
    }

    void transfer(Connectivity c, ConnectivitySet target) {
//        public void transfer(Connectivity c, MutableConnectivitySet target) {
        remove(c);
        target.add(c);
    }
}

class SingleConnectivitySet extends ConnectivitySet implements {
    void set(Connectivity c) {
        add(c);
    }
}


class ConnectivitySetGroup implements IForEach {
    //    class ConnectivitySetGroup implements ImmutableConnectivitySet {
    private ArrayList<ConnectivitySet> group = new ArrayList<>();

    ConnectivitySetGroup(ConnectivitySet a, ConnectivitySet b) {
        group.add(a);
        group.add(b);
    }

    ConnectivitySetGroup(ConnectivitySet a, ConnectivitySet b, ConnectivitySet c) {
        this(a, b);
        group.add(c);
    }

    public ArrayList<ConnectivitySet> sets() {
        return group;
    }

    public void forEach(Consumer<Connectivity> fn) {
        group.forEach(set -> set.forEach(fn));
    }

    public void broadcast(Object obj) {
        group.forEach(set -> set.broadcast(obj));
    }

    ConnectivitySet owner(Connectivity c) {
        for (ConnectivitySet set : group) {
            if (set.contains(c)) {
                return set;
            }
        }
        return null;
    }

    ConnectivitySet exclude(Connectivity toExclude) {
        return ConnectivitySet.exclude(this, toExclude);
    }
}
