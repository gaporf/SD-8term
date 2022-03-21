package report;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class Main {
    public static void main(final String[] args) {
        final HttpServer httpServer;

        try {
            httpServer = HttpServer.create(new InetSocketAddress(34401), 0);
        } catch (final Exception e) {
            System.err.println("Can't create server: " + e.getMessage());
            return;
        }
        httpServer.createContext("/get_statistics", new ReportStatistics());
        httpServer.setExecutor(null);
        httpServer.start();
    }
}
