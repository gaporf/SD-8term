package events;

import clock.SystemClock;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.Database;
import database.MembershipEvent;
import server.ServerConfig;
import server.ServerUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class RenewMembership implements HttpHandler {
    private final ServerConfig eventsConfig;
    private final Database database;

    public RenewMembership(final ServerConfig eventsConfig, final Database database) {
        this.eventsConfig = eventsConfig;
        this.database = database;
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final OutputStream outputStream = exchange.getResponseBody();
        int returnCode;
        String response;
        try {
            final Map<String, String> queryParameters = ServerUtils.getMapQuery(exchange, List.of("membership_id", "event_id", "valid_till", "password"));
            if (!eventsConfig.getPassword().equals(queryParameters.get("password"))) {
                throw new EventsException("Password is incorrect");
            }
            final int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            final int eventId = ServerUtils.parseInt(queryParameters.get("event_id"));
            final int valid_till = ServerUtils.parseInt(queryParameters.get("valid_till"));
            try {
                database.addMembershipEvent(new MembershipEvent(eventId, membershipId, valid_till, new SystemClock().now().getEpochSecond()));
                response = "MembershipEvent for membership with id = " + membershipId + " and with event id = " + eventId + " was added";
            } catch (final Exception e) {
                response = "Can't renew membership: " + e.getMessage();
            }
            returnCode = 200;
        } catch (final Exception e) {
            response = e.getMessage();
            returnCode = 400;
        }
        exchange.sendResponseHeaders(returnCode, response.length());
        outputStream.write(response.getBytes(StandardCharsets.UTF_8));
        outputStream.close();
    }
}
