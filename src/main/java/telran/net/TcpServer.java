package telran.net;

import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpServer implements Runnable {
    private ExecutorService connections = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private Protocol protocol;
    private int port;

    public TcpServer(Protocol protocol, int port) {
        this.protocol = protocol;
        this.port = port;
    }

    @Override
    public void run() {
        new Thread(this::runServer).start();;
    }

    private void runServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on the port " + port);
            serverSocket.setSoTimeout(500);
            while (!connections.isShutdown()) {
                try {
                    Socket socket = serverSocket.accept();
                    var session = new TcpClientServerSession(protocol, socket);
                    connections.execute(session);
                } catch (SocketTimeoutException e) {
                    // check shutdown
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void shutdown() {
        connections.shutdownNow();
    }
}