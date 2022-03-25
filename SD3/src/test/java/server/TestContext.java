package server;

import com.sun.net.httpserver.HttpHandler;

public class TestContext {
    private final String path;
    private final HttpHandler handler;

    public TestContext(final String path, final HttpHandler handler) {
        this.path = path;
        this.handler = handler;
    }

    public String getPath() {
        return path;
    }

    public HttpHandler getHandler() {
        return handler;
    }
}
