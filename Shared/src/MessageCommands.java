enum MessageCommands {
    LOGOUT,
    // info
    INVALID_MESSAGE,
    AUTHENTICATION_FAIL,
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    REGISTER_FAILED,
    REGISTER_SUCCESS,
    // secret
    AUTHENTICATE,
    // secret, username
    LOGIN,
    REGISTER,
    LOCK_REQUEST,
    LOCK_DENIED,
    LOCK_ALLOWED,
    // secret, username, activity
    ACTIVITY_MESSAGE,
    // activity
    ACTIVITY_BROADCAST,
    // hostname, port
    REDIRECT,
    // hostname, port, id, load
    SERVER_ANNOUNCE;

    public static boolean contains(String command) {
        for (MessageCommands c : MessageCommands.values()) {
            if (c.name().equals(command)) {
                return true;
            }
        }
        return false;
    }
}
