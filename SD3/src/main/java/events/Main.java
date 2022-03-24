package events;

import com.sun.net.httpserver.HttpServer;
import database.Database;
import server.ServerConfig;

import java.net.InetSocketAddress;

public class Main {
    public static void main(final String[] args) {
        final Database database = new Database("gym", "--drop-old-tables");
        final ServerConfig eventsConfig = new ServerConfig("C:\\Users\\gapor\\ITMO\\SD_2term\\SD3\\src\\main\\resources\\server_configs\\events.conf");
        final ServerConfig reportConfig = new ServerConfig("C:\\Users\\gapor\\ITMO\\SD_2term\\SD3\\src\\main\\resources\\server_configs\\report.conf");
        final HttpServer httpServer;
        try {
            httpServer = HttpServer.create(new InetSocketAddress(eventsConfig.getPort()), 0);
        } catch (final Exception e) {
            System.err.println("Can't create server: " + e.getMessage());
            return;
        }
        httpServer.createContext("/register_membership", new RegisterMemberhip(eventsConfig, reportConfig, database));
        httpServer.createContext("/renew_membership", new RenewMembership(eventsConfig, database));
        httpServer.createContext("/get_memberships", new GetMemberships(eventsConfig, database));
        httpServer.createContext("/get_membership_events", new GetMembershipEvents(eventsConfig, database));
        httpServer.createContext("/new_turnstile_event", new NewTurnstileEvent(eventsConfig, reportConfig, database));
        httpServer.createContext("/get_turnstile_events", new GetTurnstileEvents(eventsConfig, database));
        httpServer.setExecutor(null);
        httpServer.start();
    }
}
