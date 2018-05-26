import java.util.function.Supplier;

@SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
class Util {
    static Thread async(Runnable target) {
        Thread t = new Thread(target);
        t.start();
        return t;
    }

    static boolean wrapException(Runnable fn) {
        try {
            fn.run();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static void retry(Supplier<Boolean> fn, int interval, int timeout) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeout;
        while (fn.get()) {
            Thread.sleep(interval);
            long currentTime = System.currentTimeMillis();
            if (currentTime >= endTime) {
                break;
            }
        }
    }

    static void retry(Runnable fn, int interval, int timeout) throws InterruptedException {
        retry(() -> wrapException(fn), interval, timeout);
    }
}
