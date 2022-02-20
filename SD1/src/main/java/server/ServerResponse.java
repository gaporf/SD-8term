package server;

import java.util.List;

public class ServerResponse {
    private final String serverName;
    private final String request;
    private final List<String> urls;

    public ServerResponse(final String serverName, final String request, final List<String> urls) {
        this.serverName = serverName;
        this.request = request;
        this.urls = urls;
    }

    public String getServerName() {
        return serverName;
    }

    public String getRequest() {
        return request;
    }

    public List<String> getUrls() {
        return urls;
    }
}
