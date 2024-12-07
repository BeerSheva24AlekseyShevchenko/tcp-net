package telran.net;

import java.net.*;

import telran.net.exceptions.SocketDDoSException;
import telran.net.exceptions.SocketNegativeLimitException;

import java.io.*;

public class TcpClientServerSession implements Runnable {
    private final int NEGATIVE_RETRIES_LIMIT = 3;
    private final int REQUESTS_PER_SECOND_LIMIT = 2;
    private Protocol protocol;
    private Socket socket;

    private int negativeCount = 0;
    private int callCount = 0;
    private long lastCallTime;

    public TcpClientServerSession(Protocol protocol, Socket socket) {
        this.protocol = protocol;
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintStream writer = new PrintStream(socket.getOutputStream())) {
            socket.setSoTimeout(500);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String request = reader.readLine();
                    Response response = protocol.getResponse(request);
                    registerResponse(response);
                    writer.println(response);
                } catch (SocketTimeoutException e) {
                    if (negativeCount >= NEGATIVE_RETRIES_LIMIT) throw new SocketNegativeLimitException();
                    if (callCount >= REQUESTS_PER_SECOND_LIMIT) throw new SocketDDoSException();
                }
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerResponse(Response response) {
        if (response.isSuccess()) {
            negativeCount = 0;
        } else {
            negativeCount++;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCallTime >= 1000) {
            callCount = 1;
            lastCallTime = currentTime;
        } else {
            callCount++;
        }
    }

}