import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

public class RemoteMethod extends UnicastRemoteObject implements IRemoteMath{

    RemoteMethod() throws RemoteException {
    }

    @Override
    public String authenticate(String secret) throws RemoteException {
        return null;
    }

    @Override
    public String updateServerAdd(String hostname, Integer port) throws RemoteException {
        return null;
    }

    @Override
    public Integer getLoad() throws RemoteException {
        return null;
    }

    @Override
    public String userList(Map<String, String> user) throws RemoteException {
        return null;
    }

    @Override
    public String activityBroadcast(Object msg) throws RemoteException {
        return null;
    }

    @Override
    public String lockRequest(String username, String secret) throws RemoteException {
        return null;
    }

    @Override
    public String userUpdate(String username, String secret) throws RemoteException {
        return null;
    }

    @Override
    public String userLogout(String username) throws RemoteException {
        return null;
    }

    @Override
    public String userLogin(String username) throws RemoteException {
        return null;
    }
}
