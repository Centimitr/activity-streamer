import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.function.Consumer;

@SuppressWarnings({"Duplicates", "WeakerAccess"})
public class Connectivity extends Thread {

    private static final Logger log = LogManager.getLogger();
    private Gson g = new Gson();
    private BufferedReader in;
    private BufferedWriter out;
    private boolean open;
    private Socket socket;

    Connectivity(String hostname, int port) throws IOException {
        socket = new Socket(hostname, port);
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
            fetch(inputStr + "\n", reply -> {
                System.out.println("RES: " + reply);
                System.out.print("REQ: ");
            });
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

    public boolean send(Object src) {
        return this.send(g.toJson(src) + "\n");
    }

    public boolean fetch(String msg, Consumer<String> callback) {
        boolean ok = send(msg);
        if (!ok) {
            return false;
        }
        try {
            String reply = in.readLine();
            callback.accept(reply);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public <T> boolean fetch(Object src, Class<T> classOfT, Consumer<T> callback) {
        boolean ok = send(src);
        if (!ok) {
            return false;
        }
        try {
            String reply = in.readLine();
            T result = g.fromJson(reply, classOfT);
            callback.accept(result);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
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
