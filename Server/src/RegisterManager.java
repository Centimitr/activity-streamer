import java.util.ArrayList;
import java.util.concurrent.Semaphore;

class RegisterRequest {
    public String username;
    public String secret;
    private Semaphore sema;
    private int permits;

    RegisterInfo(String username, String secret, int permits) {
        this.username = username;
        this.secret = secret;
        this.permits = permits;
        this.sema = new Semaphore(permits);
    }

    boolean match(String username, String secret) {
        return this.username.equals(username) && this.secret.equals(secret);
    }

    public void wait() {
        try {
            sema.acquire(permits);
        } catch (InterruptedException ignored) {
        }
    }

    public void release() {
        sema.release();
    }

    public void releaseAll() {
        sema.release(sema.availablePermits());
    }
}

class RegisterManager {
    private ArrayList<RegisterRequest> requests = new ArrayList<>();

    boolean wait(String username, String secret, int num) {
        RegisterRequest req = new RegisterRequest(username, secret, num);
        requests.add(req);
        req.wait();
    }

    RegisterRequest get(String username, String secret) {
        for (RegisterRequest req : requests) {
            if (req.match(username, secret)) {
                return req;
            }
        }
    }
}
