import java.util.HashMap;
import java.util.Map;

class Account {
    String registerSourceId;
    String username;
    String secret;

    Account(String registerSourceId, String username, String secret) {
        this.registerSourceId = registerSourceId;
        this.username = username;
        this.secret = secret;
    }
}

class RegisterManager {
    private Map<String, Account> accounts = new HashMap<>();

    boolean has(String username) {
        return accounts.containsKey(username);
    }

//    boolean add(String username, String secret) {
//        if (has(username)) {
//            return false;
//        }
//        Account account = new Account(username, secret);
//        accounts.put(username, account);
//        return true;
//    }

    void put(String registerSourceId, String username, String secret) {
        Account candidate = new Account(registerSourceId, username, secret);
        if (has(username)) {
            Account existed = accounts.get(username);
            if (candidate.registerSourceId.compareTo(existed.registerSourceId) >= 0) {
                return;
            }
        }
        accounts.put(username, candidate);
    }

    boolean delete(String username, String secret) {
        return accounts.remove(username, secret);
    }

    boolean match(String username, String secret) {
        if (!has(username)) {
            return false;
        }
        if (secret == null) {
            return false;
        }
        Account account = accounts.get(username);
        return secret.equals(account.secret);
    }
}
