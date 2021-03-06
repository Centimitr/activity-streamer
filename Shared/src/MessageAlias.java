import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

class MsgLogout extends Message {
    MsgLogout() {
        super(MessageCommands.LOGOUT);
    }
}

// info
class MsgInvalidMessage extends MessageInfo {
    MsgInvalidMessage(String info) {
        super(MessageCommands.INVALID_MESSAGE, info);
    }
}

class MsgAuthenticationFail extends MessageInfo {
    MsgAuthenticationFail(String info) {
        super(MessageCommands.AUTHENTICATION_FAIL, info);
    }
}

class MsgAuthenticationSucess extends MessageInfo {
    MsgAuthenticationSucess(String info) {
        super(MessageCommands.AUTHENTICATION_SUCCESS, info);
    }
}

class MsgClientAnnounce extends MessageInfo {
    MsgClientAnnounce(String info) {
        super(MessageCommands.CLIENT_ANNOUNCE, info);
    }
}

class MsgMasterAnnounce extends MessageInfo {
    MsgMasterAnnounce(String info) {
        super(MessageCommands.MASTER_ANNOUNCE, info);
    }
}

class MsgSystemBusy extends MessageInfo {
    MsgSystemBusy(String info) {
        super(MessageCommands.SYSTEM_BUSY, info);
    }
}

class MsgLoadRequire extends MessageInfo {
    MsgLoadRequire(String info) {
        super(MessageCommands.LOAD_REQUIRE, info);
    }
}

class MsgLoginSuccess extends MessageInfo {
    MsgLoginSuccess(String info) {
        super(MessageCommands.LOGIN_SUCCESS, info);
    }
}

class MsgLoginFailed extends MessageInfo {
    MsgLoginFailed(String info) {
        super(MessageCommands.LOGIN_FAILED, info);
    }
}

class MsgRegisterFailed extends MessageInfo {
    MsgRegisterFailed(String info) {
        super(MessageCommands.REGISTER_FAILED, info);
    }
}

class MsgRegisterSuccess extends MessageInfo {
    MsgRegisterSuccess(String info) {
        super(MessageCommands.REGISTER_SUCCESS, info);
    }
}

// secret
class MsgSlaveAuthenticate extends MessageSecret {
    MsgSlaveAuthenticate(String secret) {
        super(MessageCommands.SLAVE_AUTHENTICATE, secret);
    }
}

class MsgGatewayAuthenticate extends MessageSecret {
    MsgGatewayAuthenticate(String secret) {
        super(MessageCommands.GATEWAY_AUTHENTICATE, secret);
    }
}

// secret, username
class MsgLogin extends MessageUser {
    MsgLogin(String username, String secret) {
        super(MessageCommands.LOGIN, secret, username);
    }
}

class MsgRegister extends MessageUser {
    MsgRegister(String username, String secret) {
        super(MessageCommands.REGISTER, secret, username);
    }
}

class MsgLockRequest extends MessageUser {
    MsgLockRequest(String username, String secret) {
        super(MessageCommands.LOCK_REQUEST, secret, username);
    }
}

class MsgLockDenied extends MessageUser {
    MsgLockDenied(String username, String secret) {
        super(MessageCommands.LOCK_DENIED, secret, username);
    }
}

class MsgLockAllowed extends MessageUser {
    MsgLockAllowed(String username, String secret) {
        super(MessageCommands.LOCK_ALLOWED, secret, username);
    }
}

class MsgUserUpdate extends MessageUser {
    MsgUserUpdate(String username, String secret) {
        super(MessageCommands.USER_UPDATE, secret, username);
    }
}

// secret, username, server_id
class MsgUserLogout extends MessageUserStatus {
    MsgUserLogout(String username, String secret, int id) {
        super(MessageCommands.USER_LOGOUT, secret, username, id);
    }
}

class MsgUserLogin extends MessageUserStatus {
    MsgUserLogin(String username, String secret, int id) {
        super(MessageCommands.USER_LOGIN, secret, username, id);
    }
}

// map
class MsgLoadUpdate extends MessageServerList{
    MsgLoadUpdate(Map<Integer,Integer> load_info){
        super(MessageCommands.LOAD_UPDATE, load_info);
    }
}

class MsgUserList extends MessageUserList{
    MsgUserList(Map<String,String> users){
        super(MessageCommands.USER_LIST, users);
    }
}

//class ObjectJsonObjectConverter {
//    static final Gson g = new Gson();
//
//    static JsonObject convert(Object obj) {
//        JsonElement elm = g.toJsonTree(obj);
//        return (JsonObject) elm;
//    }
//}

// secret, username, activity
class MsgActivityMessage extends MessageActivity {
    MsgActivityMessage(String username, String secret, Map<Object, Object> activity) {
        super(MessageCommands.ACTIVITY_MESSAGE, secret, username, activity);
    }
}

class MsgActivityBroadcast extends MessageActivityBroadcast {
    MsgActivityBroadcast(Map<Object, Object> activity) {
        super(MessageCommands.ACTIVITY_BROADCAST, activity);
    }
}

// hostname, port
class MsgRedirect extends MessageServer {
    MsgRedirect(String hostname, Integer port) {
        super(MessageCommands.REDIRECT, hostname, port);
    }
}

// hostname, port, id, load
//class MsgServerAnnounce extends MessageServerAnnounce {
//    MsgServerAnnounce(String id, String hostname, Integer port, Integer load) {
//        super(MessageCommands.SERVER_ANNOUNCE, hostname, port, id, load);
//    }
//}
