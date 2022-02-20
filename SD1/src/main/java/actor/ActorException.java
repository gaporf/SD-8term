package actor;

public class ActorException extends RuntimeException {
    public ActorException(final String message, final Exception e) {
        super(message, e);
    }

    public ActorException(final String message) {
        super(message);
    }
}
