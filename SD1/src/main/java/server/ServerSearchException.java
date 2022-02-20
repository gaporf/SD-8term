package server;

public class ServerSearchException extends RuntimeException {
    public ServerSearchException(final String message) {
        super(message);
    }

    public ServerSearchException(final String message, final Exception e) {
        super(message, e);
    }
}
