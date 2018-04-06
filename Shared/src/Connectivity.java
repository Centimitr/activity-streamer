import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@SuppressWarnings({"Duplicates", "WeakerAccess"})
public class Connectivity extends Thread {

    private static final Logger log = LogManager.getLogger();
    private Gson g = new Gson();
    public BufferedReader in;
    public BufferedWriter out;
    private boolean open;
    private Socket socket;
    private Consumer<Connectivity> fn;


    Connectivity(String hostname, int port, Consumer<Connectivity> fn) throws IOException {
        this(new Socket(hostname, port), fn);
    }

    Connectivity(Socket socket, Consumer<Connectivity> fn) throws IOException {
        this.socket = socket;
        this.fn = fn;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        open = true;
        log.info("Connection established");
        start();
    }

    public void run() {
        fn.accept(this);
    }

    public boolean send(String msg) {
        if (open) {
            try {
                out.write(msg);
                out.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean sendln(String msg) {
        return send(msg + "\n");
    }

    public boolean sendln(Object src) {
        return this.sendln(g.toJson(src));
    }

    public String receiveln() throws IOException {
        return in.readLine();
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
        boolean ok = sendln(src);
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

    public void redirect(BiFunction<Connectivity, String, Boolean> process) throws IOException {
        boolean term = false;
        String msg;
        while (!term && (msg = in.readLine()) != null) {
            term = process.apply(this, msg);
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
