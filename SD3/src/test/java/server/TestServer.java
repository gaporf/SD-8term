package server;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.List;

public class TestServer {
    private final HttpServer httpServer;

    public TestServer(final ServerConfig testConfig, final List<TestContext> contexts) {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(testConfig.getPort()), 0);
        } catch (final Exception e) {
            throw new TestServerException("Can't create server: " + e.getMessage(), e);
        }
        for (final TestContext context : contexts) {
            httpServer.createContext(context.getPath(), context.getHandler());
        }
        httpServer.setExecutor(null);
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }
}
