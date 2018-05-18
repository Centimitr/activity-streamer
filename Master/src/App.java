import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

public class App {

    public static void main(String args[]){

        System.setSecurityManager(new RMISecurityManager());

        try {
            RemoteMethod serverContact = new RemoteMethod();
            Naming.rebind("Gateway", serverContact);
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

}
