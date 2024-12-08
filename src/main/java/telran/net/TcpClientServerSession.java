package telran.net;

import java.net.*;

import telran.net.exceptions.SocketDDoSException;
import telran.net.exceptions.SocketNegativeLimitException;

import java.io.*;

public class TcpClientServerSession implements Runnable {
    private final int NEGATIVE_RETRIES_LIMIT = 3;
    private final int REQUESTS_PER_SECOND_LIMIT = 2;
    private final int WAIT_TIME_LIMIT = 60000;

    private Protocol protocol;
    private Socket socket;

    private int negativeCount = 0;
    private int callCount = 0;
    private long callTime;

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
            callTime = System.currentTimeMillis();
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String request = reader.readLine();
                    checkRequestFrequency();

                    Response response = protocol.getResponse(request);
                    registerRequest(response);
                    writer.println(response);
                } catch (SocketTimeoutException e) {
                    checkNegativeLimit();
                    checkTimeout();
                }
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkNegativeLimit() throws SocketNegativeLimitException {
        if (negativeCount >= NEGATIVE_RETRIES_LIMIT) throw new SocketNegativeLimitException();
    }

    private void checkRequestFrequency() throws SocketDDoSException {
        if (callCount >= REQUESTS_PER_SECOND_LIMIT) throw new SocketDDoSException();
    }

    private void checkTimeout() throws SocketTimeoutException {
        long currentTime = System.currentTimeMillis();
        if (currentTime - callTime > WAIT_TIME_LIMIT) throw new SocketTimeoutException();
    }

    private void registerRequest(Response response) {
        if (response.isSuccess()) {
            negativeCount = 0;
        } else {
            negativeCount++;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - callTime >= 1000) {
            callCount = 1;
            callTime = currentTime;
        } else {
            callCount++;
        }
    }

}