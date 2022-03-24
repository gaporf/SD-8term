package database;

public class DatabaseException extends RuntimeException {
    public DatabaseException(final String message) {
        super(message);
    }

    public DatabaseException(final String message, final Exception e) {
        super(message, e);
    }
}
