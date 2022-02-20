package server;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class GoodTestServer {
    final HttpServer httpServer;
    public GoodTestServer(final ServerConfig serverConfig) {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(serverConfig.getPort()), 0);
        } catch (final Exception e) {
            throw new ServerSearchException("Can't create server", e);
        }
        httpServer.createContext(serverConfig.getPath(), new GoodTestHandler(serverConfig));
        httpServer.setExecutor(null);
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(1);
    }
}
