package report;

import com.sun.net.httpserver.HttpServer;
import server.ServerConfig;

import java.net.InetSocketAddress;

public class Main {
    public static void main(final String[] args) {
        final ServerConfig reportConfig = new ServerConfig("C:\\Users\\gapor\\ITMO\\SD_2term\\SD3\\src\\main\\resources\\server_configs\\report.conf");
        final ServerConfig eventsConfig = new ServerConfig("C:\\Users\\gapor\\ITMO\\SD_2term\\SD3\\src\\main\\resources\\server_configs\\events.conf");
        final ReportLocalStorage reportLocalStorage = new ReportLocalStorage(eventsConfig);

        final HttpServer httpServer;

        try {
            httpServer = HttpServer.create(new InetSocketAddress(reportConfig.getPort()), 0);
        } catch (final Exception e) {
            System.err.println("Can't create server: " + e.getMessage());
            return;
        }
        httpServer.createContext("/get_statistics", new ReportStatistics(reportLocalStorage));
        httpServer.createContext("/enter", new ReportEnter(reportConfig, reportLocalStorage));
        httpServer.createContext("/exit", new ReportExit(reportConfig, reportLocalStorage));
        httpServer.createContext("/add_membership", new AddMembership(reportConfig, reportLocalStorage));
        httpServer.setExecutor(null);
        httpServer.start();
    }
}
