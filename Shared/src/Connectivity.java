import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@SuppressWarnings({"Duplicates", "WeakerAccess"})
public class Connectivity extends Thread {

    private static final Logger log = LogManager.getLogger();
    private Gson g = new Gson();
    private BufferedReader in;
    private BufferedWriter out;
    private boolean open;
    private Socket socket;
    private MessageContext context;
    private ArrayList<Runnable> whenClosedCallbacks = new ArrayList<>();

    Connectivity(String hostname, int port) throws IOException {
        this(new Socket(hostname, port));
    }

    Connectivity(Socket socket) throws IOException {
        socket.setSoTimeout(0);
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        open = true;
        log.info("[Conn::Established] " + Settings.socketAddress(socket));
        whenClosed(() -> log.info("[Conn::Closed] " + Settings.socketAddress(socket)));
        start();
    }

    public Socket socket() {
        return socket;
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

    // todo: used for test function
    @SuppressWarnings("UnusedReturnValue")
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

    // todo: not useful now, may be removed
    @SuppressWarnings("unused")
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

    // todo: improve process, maybe close in stream inside
    public boolean redirect(BiFunction<Connectivity, String, Boolean> process) {
        boolean term = false;
        String msg;
        try {
            while (!term && (msg = in.readLine()) != null) {
                term = process.apply(this, msg);
            }
            return true;
        } catch (IOException e) {
            log.error("REDIRECT: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public void bindRouter(IMessageRouter router) {
        if (context == null) {
            context = new MessageContext(router);
        } else {
            context.bindRouter(router);
        }
    }

    public boolean redirect(IMessageRouter router) {
        bindRouter(router);
        return redirect((conn, msg) -> context.process(conn, msg));
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    // todo: check if only close in stream is necessary
    public void closeIn() {
        if (open) {
            try {
                in.close();
            } catch (IOException e) {
                log.error("received exception closing the connection " + Settings.socketAddress(socket) + ": " + e);
            }
        }
    }

    public void close() {
        if (open) {
            log.info("closing connection " + Settings.socketAddress(socket));
            try {
                open = true;
                in.close();
                out.close();
                whenClosedCallbacks.forEach(Runnable::run);
            } catch (IOException e) {
                // already closed?
                log.error("received exception closing the connection " + Settings.socketAddress(socket) + ": " + e);
            }
        }
    }

    public void whenClosed(Runnable fn) {
        if (fn != null) {
            whenClosedCallbacks.add(0, fn);
        }
    }

    public boolean isOpen() {
        return open;
    }
}
