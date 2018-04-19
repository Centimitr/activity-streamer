import java.io.*;
import java.util.Scanner;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

@SuppressWarnings("WeakerAccess")
public class Client extends Thread {
    private static final Logger log = LogManager.getLogger();
    private static final Gson g = new Gson();
    private static Client clientSolution;

    public static Client getInstance() {
        if (clientSolution == null) {
            clientSolution = new Client();
        }
        return clientSolution;
    }

    public static ClientAgent getAgent() {
        return getInstance().agent;
    }

    private MessageRouter router = new MessageRouter();
    private ClientAgent agent = new ClientAgent();
    private Connectivity connectivity;
    private TextFrame textFrame;

    public Client() {
        // todo: add gui features
//        textFrame = new TextFrame();
        setMessageHandlers();
        start();
    }


    private void setMessageHandlers() {
        // todo: add protocol logic
        router
                .registerHandler(MessageCommands.REDIRECT, context -> {
                    MessageServer m = context.read(MessageServer.class);
                    log.info("Will Redirect: " + m.hostname + " " + m.port);
                    agent.reconnect(m.hostname, m.port);
                    context.close();
                })
                .registerHandler(MessageCommands.LOGIN_SUCCESS, context -> {
                    log.info("Login successfully!");
                })
                .registerHandler(MessageCommands.REGISTER_SUCCESS, context -> {
                    log.info("Register successfully!");
                })
                .registerHandler(MessageCommands.REGISTER_FAILED, context -> {
                    log.info("Register Failed!");
                })
                .registerHandler(MessageCommands.ACTIVITY_BROADCAST, context -> {
                    MessageActivityBroadcast m = context.read(MessageActivityBroadcast.class);
                    log.info("AM: " + g.toJson(m.activity));
                    System.out.println(g.toJson(m.activity));
                    // todo: may need to apply filter of sending activity objects
                })
//                .registerHandler(MessageCommands.ACTIVITY_BROADCAST, context -> {
//                    log.info("?? RCV: ACTIVITY_BROADCAST");
//                })
                .registerErrorHandler(context -> {

                });
    }

    public void disconnect() {
        if (connectivity != null) {
            connectivity.close();
        }
    }

    @Override
    public void run() {
        connect(Settings.getRemoteHostname(), Settings.getRemotePort());
    }

    private void connect(String hostname, Integer port) {
        try {
            connectivity = new Connectivity(hostname, port, c -> agent.bind(c));
            agent.login("xiaoming1", "fjskl");
            agent.sendActivity(new Message("Never Gonna Give You Up"));
            (new Thread(() -> {
                boolean closed = connectivity.redirect(router);
                if (closed) {
                    agent.unbind();
                }
                if (closed && agent.needReconnect()) {
                    connect(agent.reconnectHostname, agent.reconnectPort);
                }
            })).start();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("connect failed.");
        }
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
