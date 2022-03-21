package server;

public class ServerUtilsException extends RuntimeException {
    public ServerUtilsException(final String message) {
        super(message);
    }

    public ServerUtilsException(final String message, final Exception e) {
        super(message, e);
    }
}
