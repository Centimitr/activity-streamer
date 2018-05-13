enum MessageCommands {
    LOGOUT,
    // info
    INVALID_MESSAGE,
    AUTHENTICATION_FAIL,
    AUTHENTICATION_SUCCESS,
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    REGISTER_FAILED,
    REGISTER_SUCCESS,
    CLIENT_ANNOUNCE,
    MASTER_ANNOUNCE,
    SYSTEM_BUSY,
    LOAD_REQUIRE,
    // secret
    SLAVE_AUTHENTICATE,
    GATEWAY_AUTHENTICATE,
    // secret, username
    LOGIN,
    REGISTER,
    LOCK_REQUEST,
    LOCK_DENIED,
    LOCK_ALLOWED,
    USER_UPDATE,
    // secret, username, activity
    ACTIVITY_MESSAGE,
    // activity
    ACTIVITY_BROADCAST,
    // hostname, port
    REDIRECT,
    // username, secret, id
    USER_LOGOUT,
    USER_LOGIN,
    // map(server id, load)
    LOAD_UPDATE,
    // map(username, secret)
    USER_LIST,

    ;

    public static boolean contains(String command) {
        for (MessageCommands c : MessageCommands.values()) {
            if (c.name().equals(command)) {
                return true;
            }
        }
        return false;
    }
}
