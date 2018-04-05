class Message {
    String command;
}

class MessageSecret extends Message {
    String secret;
}

class MessageInfo extends Message {
    String info;
}

class MessageUser extends MessageSecret {
    String username;
}

class MessageActivity extends MessageUser {
    String activity;
}

class MessageServer extends Message {
    String hostname;
    String port;
}

class MessageServerAnnounce extends MessageServer {
    String id;
    Integer load;
}

class MessageActivityBroadcast extends Message {
    String activity;
}
