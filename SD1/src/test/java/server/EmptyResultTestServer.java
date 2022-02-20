package server;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class EmptyResultTestServer {
    final HttpServer httpServer;
    public EmptyResultTestServer(final ServerConfig serverConfig) {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(serverConfig.getPort()), 0);
        } catch (final Exception e) {
            throw new ServerSearchException("Can't create server", e);
        }
        httpServer.createContext(serverConfig.getPath(), new EmptyResultTestHandler(serverConfig));
        httpServer.setExecutor(null);
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(1);
    }
}
