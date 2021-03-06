import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.BlockingDeque;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@SuppressWarnings({"Duplicates", "WeakerAccess"})
public class Connectivity extends Thread {
    private static final Logger log = LogManager.getLogger();
    private static Gson g = new Gson();

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    private boolean redirecting = false;
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
        log.info("Established: " + Settings.socketAddress(socket));
        start();
    }

    public Socket socket() {
        return socket;
    }

    public boolean send(String msg) {
        if (!socket.isClosed()) {
            try {
                out.write(msg);
                out.flush();
                return true;
            } catch (IOException e) {
                log.error("Send Failed");
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean sendln(String msg) {
        log.debug("Sendln: " + msg);
        return send(msg + "\n");
    }

    public boolean sendln(Object src) {
        return this.sendln(g.toJson(src));
    }

    public String receiveln() throws IOException {
        String line = in.readLine();
        log.debug("Receiveln: " + line);
        return line;
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

    public boolean isRedirecting() {
        return redirecting;
    }

    public boolean redirect(BiFunction<Connectivity, String, Boolean> process) {
        redirecting = true;
        boolean term = false;
        String msg;
        try {
            while (!term && (msg = in.readLine()) != null) {
                log.info("Receiveln: " + msg);
                term = process.apply(this, msg);
            }
        } catch (IOException e) {
            log.error("Redirect: " + e.getMessage());
            e.printStackTrace();
        }
        close();
        return term;
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

    public void close() {
        if (!socket.isClosed()) {
            log.info("Closing: " + Settings.socketAddress(socket));
            try {
                in.close();
                out.close();
                socket.close();
                whenClosedCallbacks.forEach(Runnable::run);
                log.info("Closed: " + Settings.socketAddress(socket));
            } catch (IOException e) {
//                 already closed?
                log.error("Exception: when closing " + Settings.socketAddress(socket) + ": " + e);
            }
        }
    }

    public void whenClosed(Runnable fn) {
        if (fn != null) {
            whenClosedCallbacks.add(0, fn);
        }
    }
}
