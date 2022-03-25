package events;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.Membership;
import database.MembershipEvent;
import database.Database;
import server.ServerConfig;
import server.ServerUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class GetMembershipEvents implements HttpHandler {
    private final ServerConfig eventsConfig;
    private final Database database;

    public GetMembershipEvents(final ServerConfig eventsConfig, final Database database) {
        this.eventsConfig = eventsConfig;
        this.database = database;
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final OutputStream outputStream = exchange.getResponseBody();
        int returnCode;
        String response;
        try {
            final Map<String, String> queryParameters = ServerUtils.getMapQuery(exchange, List.of("password", "membership_id"));
            if (!eventsConfig.getPassword().equals(queryParameters.get("password"))) {
                throw new EventsException("Password is incorrect");
            }
            final int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            try {
                final Membership membership = database.getMembership(membershipId);
                final List<MembershipEvent> events = database.getMembershipsEvents(membershipId);
                final StringBuilder responseBuilder = new StringBuilder();
                responseBuilder.append("Info for membership id = ").append(membership.getId()).append(System.lineSeparator());
                responseBuilder.append("Created at ").append(membership.getAddedTimeInSeconds()).append(System.lineSeparator());
                for (final MembershipEvent event : events) {
                    responseBuilder.append(event.getEventId()).append(")")
                            .append(" time: ").append(event.getAddedTimeInSeconds()).append(", ")
                            .append("valid till ").append(event.getValidTillInSeconds()).append(System.lineSeparator());
                }
                response = responseBuilder.toString();
            } catch (final Exception e) {
                response = "Can't get membership events: " + e.getMessage();
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
