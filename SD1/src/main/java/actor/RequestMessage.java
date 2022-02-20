package actor;

public class RequestMessage {
    final private String request;

    public RequestMessage(final String request) {
        this.request = request;
    }

    public String getRequest() {
        return request;
    }
}
