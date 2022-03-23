package events;

import com.sun.net.httpserver.HttpServer;
import database.SqlDataBase;
import server.ServerConfig;

import java.net.InetSocketAddress;

public class Main {
    public static void main(final String[] args) {
        final SqlDataBase database = new SqlDataBase("gym", "--drop-old-tables");
        final ServerConfig eventsConfig = new ServerConfig("C:\\Users\\gapor\\ITMO\\SD_2term\\SD3\\src\\main\\resources\\server_configs\\events.conf");
        final ServerConfig reportConfig = new ServerConfig("C:\\Users\\gapor\\ITMO\\SD_2term\\SD3\\src\\main\\resources\\server_configs\\report.conf");
        final HttpServer httpServer;
        try {
            httpServer = HttpServer.create(new InetSocketAddress(eventsConfig.getPort()), 0);
        } catch (final Exception e) {
            System.err.println("Can't create server: " + e.getMessage());
            return;
        }
        httpServer.createContext("/register_membership", new EventsRegisterMembership(eventsConfig, reportConfig, database));
        httpServer.createContext("/renew_membership", new EventsRenewMembership(eventsConfig, database));
        httpServer.createContext("/get_memberships", new EventsGetMemberships(eventsConfig, database));
        httpServer.createContext("/get_membership_events", new EventsGetMembershipEvents(eventsConfig, database));
        httpServer.createContext("/new_turnstile_event", new EventsNewTurnstileEvent(eventsConfig, reportConfig, database));
        httpServer.createContext("/get_turnstile_events", new EventsGetTurnstileEvents(eventsConfig, database));
        httpServer.setExecutor(null);
        httpServer.start();
    }
}
