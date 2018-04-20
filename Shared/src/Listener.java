import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Listener extends Thread {
    private static final Logger log = LogManager.getLogger();
    private ServerSocket serverSocket;
    private boolean term = false;
    private int port;
    private BiConsumer<Listener, Socket> fn;

    Listener(int port, BiConsumer<Listener, Socket> fn) throws IOException {
        this.port = port;
        this.fn = fn;
        serverSocket = new ServerSocket(port);
        start();
    }

    @Override
    public void run() {
        log.info("listening for new connections on " + port);
        while (!term) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
                (new Thread(() -> fn.accept(this, clientSocket))).start();
            } catch (IOException e) {
                log.info("received exception, shutting down");
                term = true;
            }
        }
    }

    void terminate() {
        this.term = true;
        interrupt();
    }

}
