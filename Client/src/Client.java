import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Client extends Thread {
    private static final Logger log = LogManager.getLogger();
    private static Client clientSolution;
    private Socket socket;
    private TextFrame textFrame;


    public static Client getInstance() {
        if (clientSolution == null) {
            clientSolution = new Client();
        }
        return clientSolution;
    }

    public Client() {
//        textFrame = new TextFrame();
        try {
            socket = new Socket(Settings.getRemoteHostname(), Settings.getRemotePort());
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        start();
    }


    @SuppressWarnings("unchecked")
    public void sendActivityObject(JSONObject activityObj) {

    }


    public void disconnect() {

    }


    public void run() {

    }


}
