import java.util.HashMap;
import java.util.Map;

class User {
    String username;
    String secret;

    User(String username, String secret) {
        this.username = username;
        this.secret = secret;
    }
}

public class Users {
    private Map<String, User> users = new HashMap<>();

    boolean has(String username) {
        return users.containsKey(username);
    }

    boolean add(String username, String secret) {
        if (has(username)) {
            return false;
        }
        User u = new User(username, secret);
        users.put(username, u);
        return true;
    }

    boolean match(String username, String secret) {
        if (!has(username)) {
            return false;
        }
        User u = users.get(username);
        return secret.equals(u.secret);
    }
}
