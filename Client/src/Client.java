import java.io.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

@SuppressWarnings("WeakerAccess")
public class Client extends Thread {
    private static final Logger log = LogManager.getLogger();
    private static Client clientSolution;
    private Connectivity connectivity;
    private TextFrame textFrame;

    public static Client getInstance() {
        if (clientSolution == null) {
            clientSolution = new Client();
        }
        return clientSolution;
    }

    public Client() {
//        textFrame = new TextFrame();
        start();
    }

    @SuppressWarnings("unchecked")
    public void sendActivityObject(JSONObject activityObj) {
        connectivity.send(activityObj);
    }


    public void disconnect() {
        connectivity.close();
    }

    public void run() {
        try {
            connectivity = new Connectivity(Settings.getRemoteHostname(), Settings.getRemotePort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
