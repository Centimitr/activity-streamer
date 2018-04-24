class SingleConnectivitySet extends ConnectivitySet {
    void set(Connectivity c) {
        add(c);
    }

    void close() {
        closeAll();
    }
}
