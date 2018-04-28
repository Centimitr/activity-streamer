import java.io.*;
import java.util.Scanner;

@SuppressWarnings("WeakerAccess")
public class Client extends ClientResponder {
    private static Client clientSolution;
//    private TextFrame gui = new TextFrame();
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
        start();
    }

    public void disconnect() {
        if (connectivity != null) {
            connectivity.close();
        }
    }

    @Override
    public void run() {
//        view.show();
        connect(Settings.getRemoteHostname(), Settings.getRemotePort());
    }

    private void connect(String hostname, Integer port) {
        Settings.setRemoteHostname(hostname);
        Settings.setRemotePort(port);
        try {
            connectivity = new Connectivity(hostname, port);
            agent.bind(connectivity);
            async(() -> {
                boolean normalExit = connectivity.redirect(router);
                if (normalExit) {
                    log.info("normal exit");
                } else {
                    log.info("exception exit");
                }
                agent.unbind();
                if (normalExit && agent.needReconnect()) {
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
        agent.uiSetLoaded(needRegister);
        if (needRegister) {
            String secret = agent.register(Settings.getUsername());
            log.info("Your secret is: " + secret);
            Settings.setSecret(secret);
        }
        agent.login(Settings.getUsername(), Settings.getSecret());
//        gui.present();
    }

    // todo: used to test connectivity, to remove
    @SuppressWarnings("unused")
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
