import com.google.gson.JsonObject;

import java.util.Map;

class TestMessage {
    String msg;

    TestMessage(String msg) {
        this.msg = msg;
    }
}

abstract class Message {
    MessageCommands command;

    Message(MessageCommands command) {
        this.command = command;
    }
}

abstract class MessageSecret extends Message {
    String secret;

    MessageSecret(MessageCommands command, String secret) {
        super(command);
        this.secret = secret;
    }
}

abstract class MessageInfo extends Message {
    String info;

    MessageInfo(MessageCommands command, String info) {
        super(command);
        this.info = info;
    }
}

abstract class MessageUser extends MessageSecret {
    String username;

    MessageUser(MessageCommands command, String secret, String username) {
        super(command, secret);
        this.username = username;
    }
}

abstract class MessageActivity extends MessageUser {
    Map<Object, Object> activity;

    MessageActivity(MessageCommands command, String secret, String username, Map<Object, Object> activity) {
        super(command, secret, username);
        this.activity = activity;
    }
}

abstract class MessageServer extends Message {
    String hostname;
    Integer port;

    MessageServer(MessageCommands command, String hostname, Integer port) {
        super(command);
        this.hostname = hostname;
        this.port = port;
    }
}

abstract class MessageServerAnnounce extends MessageServer {
    String id;
    Integer load;

    MessageServerAnnounce(MessageCommands command, String hostname, Integer port, String id, Integer load) {
        super(command, hostname, port);
        this.id = id;
        this.load = load;
    }
}

abstract class MessageActivityBroadcast extends Message {
    Map<Object, Object> activity;

    MessageActivityBroadcast(MessageCommands command, Map<Object, Object> activity) {
        super(command);
        this.activity = activity;
    }
}

