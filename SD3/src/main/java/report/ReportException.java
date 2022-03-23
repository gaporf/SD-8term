package report;

public class ReportException extends RuntimeException {
    public ReportException(final String message) {
        super(message);
    }

    public ReportException(final String message, final Exception e) {
        super(message, e);
    }
}
