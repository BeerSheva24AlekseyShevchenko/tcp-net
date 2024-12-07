package telran.net.exceptions;

import java.net.SocketException;

public class SocketNegativeLimitException extends SocketException {
    public SocketNegativeLimitException() {
        super(String.format("Exceeding the limit of negative requests"));
    }
}
