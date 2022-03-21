package turnstile;

import com.sun.net.httpserver.HttpServer;


import java.net.InetSocketAddress;

public class Main {
    public static void main(final String[] args) {
        final HttpServer httpServer;

        try {
            httpServer = HttpServer.create(new InetSocketAddress(34391), 0);
        } catch (final Exception e) {
            System.err.println("Can't create server: " + e.getMessage());
            return;
        }
        httpServer.createContext("/enter", new TurnstileEnter());
        httpServer.createContext("/exit", new TurnstileExit());
        httpServer.setExecutor(null);
        httpServer.start();
    }
}
