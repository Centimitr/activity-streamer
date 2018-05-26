// combination better than inheritance here
class LegacyLock {
    private WaitGroup wg = new WaitGroup();
    private boolean locked = false;

    boolean locked() {
        return locked;
    }

    void lock() {
        locked = true;
    }

    void lockAndWait() {
        locked = true;
        wg.wait(1);
        locked = false;
    }

    void unlock() {
        wg.done();
    }
}
