import java.io.*;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

@SuppressWarnings("WeakerAccess")
public class Client extends Thread {
    private static final Logger log = LogManager.getLogger();
    private static Client clientSolution;

    private MessageRouter router = new MessageRouter();
    private Connectivity connectivity;
    private TextFrame textFrame;

    public static Client getInstance() {
        if (clientSolution == null) {
            clientSolution = new Client();
        }
        return clientSolution;
    }

    public Client() {
        // todo: add gui features
//        textFrame = new TextFrame();
        setMessageHandlers();
        start();
    }

    private void setMessageHandlers() {
        // todo: add protocol logic
        router
                .registerHandler(MessageCommands.INVALID_MESSAGE, context -> {
                })
                .registerHandler(MessageCommands.AUTHENTICATE, context -> {

                })
                .registerErrorHandler(context -> {

                });
    }

    @SuppressWarnings("unchecked")
    public void sendActivityObject(JSONObject activityObj) {
        if (connectivity != null) {
            connectivity.sendln(activityObj);
        }
    }

    public void disconnect() {
        if (connectivity != null) {
            connectivity.close();
        }
    }

    public void run() {
        try {
//            connectivity = new Connectivity(Settings.getRemoteHostname(), Settings.getRemotePort(), this::handleTestREPL);
            connectivity = new Connectivity(Settings.getRemoteHostname(), Settings.getRemotePort(), this::handleConnection);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // todo: authentication: register, login
    private void handleConnection(Connectivity c) {
//        boolean ok = c.redirect((conn, msg) -> (new MessageContext(router)).process(conn, msg));
        boolean ok = c.redirect(router);
        // todo: close the connection
    }

    // todo: used to test connectivity, to remove
    private void handleTestREPL(Connectivity c) {
        Scanner scanner = new Scanner(System.in);
        String inputStr;

        System.out.print("REQ: ");
        while (!(inputStr = scanner.nextLine()).equals("exit")) {
            c.fetch(inputStr + "\n", reply -> {
                System.out.println("RES: " + reply);
                System.out.print("REQ: ");
            });
        }
        scanner.close();
    }
}
