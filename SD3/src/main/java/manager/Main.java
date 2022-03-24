package manager;

import com.sun.net.httpserver.HttpServer;
import server.ServerConfig;

import java.net.InetSocketAddress;

public class Main {
    public static void main(final String[] args) {
        final ServerConfig managerConfig = new ServerConfig("C:\\Users\\gapor\\ITMO\\SD_2term\\SD3\\src\\main\\resources\\server_configs\\manager.conf");
        final ServerConfig eventsConfig = new ServerConfig("C:\\Users\\gapor\\ITMO\\SD_2term\\SD3\\src\\main\\resources\\server_configs\\events.conf");
        final HttpServer httpServer;

        try {
            httpServer = HttpServer.create(new InetSocketAddress(managerConfig.getPort()), 0);
        } catch (final Exception e) {
            System.err.println("Can't create server: " + e.getMessage());
            return;
        }
        httpServer.createContext("/give_membership", new GiveMembership(eventsConfig));
        httpServer.createContext("/renew_membership", new RenewMembership(eventsConfig));
        httpServer.createContext("/membership_info", new MembershipInfo(eventsConfig));
        httpServer.setExecutor(null);
        httpServer.start();
    }
}
