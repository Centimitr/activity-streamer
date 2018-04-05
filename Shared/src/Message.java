class Message {
    String command;

    Message(String command) {
        this.command = command;
    }
}

class MessageSecret extends Message {
    String secret;

    MessageSecret(String command, String secret) {
        super(command);
        this.secret = secret;
    }
}

class MessageInfo extends Message {
    String info;

    MessageInfo(String command, String info) {
        super(command);
        this.info = info;
    }
}

class MessageUser extends MessageSecret {
    String username;

    MessageUser(String command, String secret, String username) {
        super(command, secret);
        this.username = username;
    }
}

class MessageActivity extends MessageUser {
    String activity;

    MessageActivity(String command, String secret, String username, String activity) {
        super(command, secret, username);
        this.activity = activity;
    }
}

class MessageServer extends Message {
    String hostname;
    String port;

    MessageServer(String command, String hostname, String port) {
        super(command);
        this.hostname = hostname;
        this.port = port;
    }
}

class MessageServerAnnounce extends MessageServer {
    String id;
    Integer load;

    MessageServerAnnounce(String command, String hostname, String port, String id, Integer load) {
        super(command, hostname, port);
        this.id = id;
        this.load = load;
    }
}

class MessageActivityBroadcast extends Message {
    String activity;

    MessageActivityBroadcast(String command, String activity) {
        super(command);
        this.activity = activity;
    }
}
