package server;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class FewResultsTestServer {
    final HttpServer httpServer;

    public FewResultsTestServer(final ServerConfig serverConfig, final int results) {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(serverConfig.getPort()), 0);
        } catch (final Exception e) {
            throw new ServerSearchException("Can't create server", e);
        }
        httpServer.createContext(serverConfig.getPath(), new FewResultsTestHandler(serverConfig, results));
        httpServer.setExecutor(null);
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(1);
    }
}
