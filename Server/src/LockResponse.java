import java.util.HashMap;
import java.util.Map;

class Response{
    private Connectivity server;
    private MessageUser res;

    Response(Connectivity server, MessageUser res){
        this.server = server;
        this.res = res;
    }

    public MessageUser getRes() {
        return res;
    }
}

public class LockResponses {
    private Map<Connectivity, Response> responseList = new HashMap<>();

    void update(ConnectivitySet servers){
        //todo: exception
        for (Connectivity c : servers) {
            Response res = new Response(c,null);
            responseList.put(c, res);
        }
    }

    void add (Connectivity c, MsgLockDenied res){
        Response denied = new Response(c, res);
        responseList.put(c, denied);
    }

    void add (Connectivity c, MsgLockAllowed res){
        Response allowed = new Response(c, res);
        responseList.put(c, allowed);
    }

    boolean allAnswered(){
        for (Response res: responseList.values()) {
            if (res.getRes() == null){
                return false;
            }
        }
        return true;
    }

    boolean allAllowed(){
        for (Response res : responseList.values()){
            if (res.getRes().command.equals(MessageCommands.LOCK_DENIED)){
                return false;
                break;
            }
        }
        return true;
    }
}
