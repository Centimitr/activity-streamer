import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

@SuppressWarnings("Duplicates")
public class Connectivity extends Thread {

    private static final Logger log = LogManager.getLogger();
    private Gson g = new Gson();
    private BufferedReader in;
    private BufferedWriter out;
    private boolean open;
    private Socket socket;

    Connectivity() throws IOException {
        socket = new Socket(Settings.getRemoteHostname(), Settings.getRemotePort());
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        open = true;
        log.info("Connection established");
        start();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        String inputStr;

        System.out.print("REQ: ");
        while (!(inputStr = scanner.nextLine()).equals("exit")) {
            String received = fetch(inputStr);
            System.out.println("RES: " + received);
            System.out.print("REQ: ");
        }
        scanner.close();
    }

    public boolean send(String msg) {
        if (open) {
            try {
                out.write(msg);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public boolean send(String msg, boolean newLine) {
        return this.send(msg + (newLine ? "\n" : ""));
    }

    // send(request) and fetch(request/reply) will use send with newLine
    public boolean send(Object obj) {
        return this.send(g.toJson(obj), true);
    }

    public String fetch(String msg) {
        send(msg, true);
        try {
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void close() {
        if (open) {
            log.info("closing connection " + Settings.socketAddress(socket));
            try {
                open = true;
                in.close();
                out.close();
            } catch (IOException e) {
                // already closed?
                log.error("received exception closing the connection " + Settings.socketAddress(socket) + ": " + e);
            }
        }
    }

    public boolean isOpen() {
        return open;
    }
}
