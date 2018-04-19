import java.util.ArrayList;


interface ImmutableConnectivitySet {

}

interface MutableConnectivitySet extends ImmutableConnectivitySet {
    boolean add(Connectivity c);

    boolean remove(Connectivity c);

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

    ArrayList<Connectivity> get() {
        return conns;
    }

    ConnectivitySet concat(ConnectivitySet src) {
        ConnectivitySet set = new ConnectivitySet();
        conns.forEach(set::add);
        src.conns.forEach(src::add);
        return set;
    }

    @SuppressWarnings("WeakerAccess")
    ConnectivitySet exclude(Connectivity toExclude) {
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

    ConnectivitySet exclude(MessageContext toExclude) {
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
        group.add(a);
        group.add(b);
        group.add(c);
    }
}