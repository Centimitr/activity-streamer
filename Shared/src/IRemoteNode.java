import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteNode extends Remote {

    boolean declare(String secret, String remoteHostname, int remotePort) throws RemoteException;

//    void recover() throws RemoteException;
//    void update() throws RemoteException;
}

