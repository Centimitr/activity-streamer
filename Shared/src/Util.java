import java.util.function.Supplier;

@SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
class Util {
    static Thread async(Runnable target) {
        Thread t = new Thread(target);
        t.start();
        return t;
    }

    static void retry(Supplier<Boolean> fn, int interval, int timeout) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeout;
        while (fn.get()) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                break;
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime >= endTime) {
                break;
            }
        }
    }

}
