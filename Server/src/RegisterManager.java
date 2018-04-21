import java.util.ArrayList;

class RegisterRequest extends WaitGroup {
    private String username;
    private String secret;

    RegisterRequest(String username, String secret) {
        this.username = username;
        this.secret = secret;
    }

    boolean match(String username, String secret) {
        return this.username.equals(username) && this.secret.equals(secret);
    }
}

class RegisterManager {
    private ArrayList<RegisterRequest> requests = new ArrayList<>();

    boolean wait(String username, String secret, int num) {
        RegisterRequest req = new RegisterRequest(username, secret);
        requests.add(req);
        boolean ok = req.wait(num);
        requests.remove(req);
        return ok;
    }

    RegisterRequest get(String username, String secret) {
        for (RegisterRequest req : requests) {
            if (req.match(username, secret)) {
                return req;
            }
        }
    }
}
