package telran.net.exceptions;

import java.net.SocketException;

public class SocketDDoSException extends SocketException {
    public SocketDDoSException() {
        super(String.format("DDoS attack detected"));
    }
}
