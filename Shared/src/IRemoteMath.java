import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface IRemoteMath extends Remote{
    Map<Integer, Integer> getServerStatus() throws RemoteException;

    Map<String, String> getUserList() throws RemoteException;

    String gatewayAuthenticate() throws RemoteException;

    String slaveAuthenticate() throws RemoteException;

}

