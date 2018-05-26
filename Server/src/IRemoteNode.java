import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface IRemoteNode extends Remote {

    String declare(String secret, String remoteHostname, int remotePort, boolean needRecovery) throws RemoteException;

    ArrayList<String> getUserList() throws RemoteException;

    ArrayList<String> sendMessage(String sender, ArrayList<String> receivers, String msg, boolean retry) throws RemoteException;

    void register(String id, String username, String secret) throws RemoteException;

    int getLoad() throws RemoteException;
}
