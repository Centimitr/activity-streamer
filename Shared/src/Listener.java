import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Listener extends Async {
    private static final Logger log = LogManager.getLogger();
    private ServerSocket serverSocket;
    private boolean term = false;
    private int port;
    private Consumer<Socket> fn;

    Listener(int port, Consumer<Socket> fn) throws IOException {
        this.port = port;
        this.fn = fn;
        serverSocket = new ServerSocket(port);
        start();
    }

    @Override
    public void run() {
        log.info("Listening Port: " + port);
        while (!term) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
                async(() -> fn.accept(clientSocket));
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
