package database;

public class SqlDataBaseException extends RuntimeException {
    public SqlDataBaseException(final String message) {
        super(message);
    }

    public SqlDataBaseException(final String message, final Exception e) {
        super(message, e);
    }
}
