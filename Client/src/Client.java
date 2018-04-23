import java.io.*;
import java.util.Scanner;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

@SuppressWarnings("WeakerAccess")
public class Client extends Async {
    private static final Logger log = LogManager.getLogger();
    private static final Gson g = new Gson();
    private static Client clientSolution;

    private MessageRouter router = new MessageRouter();
    private ClientAgent agent = new ClientAgent();
    private Connectivity connectivity;
    private TextFrame gui = new TextFrame();

    //    private View view = new View();
    public static Client getInstance() {
        if (clientSolution == null) {
            clientSolution = new Client();
        }
        return clientSolution;
    }

    public static ClientAgent getAgent() {
        return getInstance().agent;
    }

    public Client() {
        setMessageHandlers();
        start();
    }

    private void setMessageHandlers() {
        // todo: add protocol logic
        router
                .registerHandler(MessageCommands.REDIRECT, context -> {
                    MsgRedirect m = context.read(MsgRedirect.class);
                    log.info("Will Redirect: " + m.hostname + " " + m.port);
                    agent.reconnect(m.hostname, m.port);
                    context.close();
                })
                .registerHandler(MessageCommands.REGISTER_SUCCESS, context -> {
                    agent.registerLock.unlock();
                    log.info("Register successfully!");
                })
                .registerHandler(MessageCommands.LOGIN_SUCCESS, context -> {
                    agent.loginLock.unlock();
                    log.info("Login successfully!");
                })
                .registerHandler(MessageCommands.REGISTER_FAILED, context -> {
                    log.info("Register Failed!");
                    context.close();
                })
                .registerHandler(MessageCommands.ACTIVITY_BROADCAST, context -> {
                    MsgActivityBroadcast m = context.read(MsgActivityBroadcast.class);
                    log.info("AM: " + g.toJson(m.activity));
                    System.out.println(g.toJson(m.activity));
                    // todo: may need to apply filter of sending activity objects
                })
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
            connectivity = new Connectivity(hostname, port);
            agent.bind(connectivity);
            async(() -> {
                boolean closed = connectivity.redirect(router);
                if (closed) {
                    agent.unbind();
                }
                if (closed && agent.needReconnect()) {
                    connect(agent.reconnectHostname, agent.reconnectPort);
                }
            });
            whenConnected();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("connect failed.");
        }
    }

    private void whenConnected() {
        boolean anonymous = Settings.getUsername().equals("anonymous");
        boolean needRegister = Settings.getSecret() == null && !anonymous;
        if (needRegister) {
            String secret = agent.register(Settings.getUsername());
            log.info("Your secret is: " + secret);
            Settings.setSecret(secret);
        }
        agent.login(Settings.getUsername(), Settings.getSecret());
        gui.present();
        // agent.sendActivity(new TestMessage("Never Gonna Give You Up"));
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
