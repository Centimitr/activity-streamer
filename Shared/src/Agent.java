abstract class Agent {
    Connectivity connectivity;

    void bind(Connectivity c) {
        this.connectivity = c;
    }

    public boolean sendln(Object src) {
        return connectivity.sendln(src);
    }
}
