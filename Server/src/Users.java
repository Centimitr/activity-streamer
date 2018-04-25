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

class Users {
    private Map<String, User> users = new HashMap<>();

    boolean has(String username) {
        System.out.println("Print names");
        users.forEach((name, user) -> {
            System.out.println(name);
        });
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

    boolean delete(String username, String secret) {
        return users.remove(username, secret);
    }

    boolean match(String username, String secret) {
        if (!has(username)) {
            return false;
        }
        if (secret == null) {
            return false;
        }
        User u = users.get(username);
        System.out.println("Maybe null " + u.username + " " + u.secret);
        return secret.equals(u.secret);
    }
}
