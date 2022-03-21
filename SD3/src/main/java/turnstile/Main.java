package turnstile;

import com.sun.net.httpserver.HttpServer;
import server.ServerConfig;


import java.net.InetSocketAddress;

public class Main {
    public static void main(final String[] args) {
        final ServerConfig turnstileConfig = new ServerConfig("C:\\Users\\gapor\\ITMO\\SD_2term\\SD3\\src\\main\\resources\\server_configs\\turnstile.conf");
        final ServerConfig eventsConfig = new ServerConfig("C:\\Users\\gapor\\ITMO\\SD_2term\\SD3\\src\\main\\resources\\server_configs\\events.conf");
        final HttpServer httpServer;
        try {
            httpServer = HttpServer.create(new InetSocketAddress(turnstileConfig.getPort()), 0);
        } catch (final Exception e) {
            System.err.println("Can't create server: " + e.getMessage());
            return;
        }
        httpServer.createContext("/enter", new TurnstileEnter(eventsConfig));
        httpServer.createContext("/exit", new TurnstileExit(eventsConfig));
        httpServer.setExecutor(null);
        httpServer.start();
    }
}
