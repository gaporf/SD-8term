package server;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class SearchServer {
    private final HttpServer httpServer;

    public SearchServer(final ServerConfig serverConfig) {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(serverConfig.getPort()), 0);
        } catch (final Exception e) {
            throw new ServerSearchException("Can't create server", e);
        }
        httpServer.createContext(serverConfig.getPath(), new ServerHandler(serverConfig.getServerName(), serverConfig.getResponseFormat(), serverConfig.getTimeoutInMilliseconds()));
        httpServer.setExecutor(null);
        httpServer.start();
    }

    public void stop() {
        System.out.println("Start stopping server");
        httpServer.stop(1);
        System.out.println("Server stopped");
    }
}
