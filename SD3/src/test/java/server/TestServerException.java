package server;

public class TestServerException extends RuntimeException {
    public TestServerException(final String message, final Exception e) {
        super(message, e);
    }
}
