class Async extends Thread {
    void async(Runnable target) {
        (new Thread(target)).start();
    }
}
