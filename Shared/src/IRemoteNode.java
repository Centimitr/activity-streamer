import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteNode extends Remote {

    boolean declare(String secret, String id, String remoteHostname, int remotePort, boolean needRecovery) throws RemoteException;

    void recover(String serversSnapshot, String usersSnapshot) throws RemoteException;
}

