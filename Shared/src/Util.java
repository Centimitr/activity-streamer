
@SuppressWarnings("UnusedReturnValue")
class Util {
    static Thread async(Runnable target) {
        Thread t = new Thread(target);
        t.start();
        return t;
    }
}