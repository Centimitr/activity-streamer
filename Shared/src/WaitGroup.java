import java.util.concurrent.Semaphore;

class WaitGroup {
    private Semaphore sema;
    private boolean cancelled;

    boolean wait(int permits) {
        sema = new Semaphore(permits);
        cancelled = false;
        try {
            sema.acquire(permits);
            return !cancelled;
        } catch (InterruptedException ignored) {
        }
        return false;
    }

    public void done() {
        sema.release();
    }

    public void cancel() {
        cancelled = true;
        sema.release(sema.availablePermits());
    }
}
