import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface IRemoteMath extends Remote{
    String authenticate(String secret) throws RemoteException;

    String updateServerAdd(String hostname, Integer port) throws RemoteException;

    Integer getLoad() throws RemoteException;

    String userList(Map<String, String> user) throws RemoteException;

    String activityBroadcast(Object msg) throws RemoteException;

    // reply with a user(username, secret) and a string(allowed or denied)
    String lockRequest(String username, String secret) throws RemoteException;

    String userUpdate(String username, String secret) throws RemoteException;

    String userLogout(String username) throws RemoteException;

    String userLogin(String username) throws RemoteException;


}

