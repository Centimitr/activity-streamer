import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

public class RemoteMethod extends UnicastRemoteObject implements IRemoteMath{

    RemoteMethod() throws RemoteException {
    }

    @Override
    public Map<Integer, Integer> getServerStatus() throws RemoteException {
        return null;
    }

    @Override
    public Map<String, String> getUserList() throws RemoteException {
        return null;
    }

    @Override
    public String gatewayAuthenticate() throws RemoteException {
        return null;
    }

    @Override
    public String slaveAuthenticate() throws RemoteException {
        return null;
    }

}
