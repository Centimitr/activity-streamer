import java.io.*;
import java.util.Scanner;

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
        connectivity.sendln(activityObj);
    }


    public void disconnect() {
        connectivity.close();
    }

    public void run() {
        try {
            connectivity = new Connectivity(Settings.getRemoteHostname(), Settings.getRemotePort(), c -> {
                Scanner scanner = new Scanner(System.in);
                String inputStr;

                System.out.print("REQ: ");
                while (!(inputStr = scanner.nextLine()).equals("exit")) {
                    log.info("0");
                    c.fetch(inputStr + "\n", reply -> {
                        log.info("2");
                        System.out.println("RES: " + reply);
                        System.out.print("REQ: ");
                    });
                }
                scanner.close();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
