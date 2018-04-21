abstract class Agent {
    private Connectivity connectivity;

    void bind(Connectivity c) {
        connectivity = c;
    }

    void unbind() {
        connectivity = null;
    }

    boolean sendln(Object src) {
        if (connectivity == null) {
            return false;
        }
        return connectivity.sendln(src);
    }

    void close() {
        connectivity.close();
    }
}
