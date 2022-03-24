package events;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.Database;
import database.Membership;
import database.TurnstileEvent;
import server.ServerConfig;
import server.ServerUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class GetTurnstileEvents implements HttpHandler {
    private final ServerConfig eventsConfig;
    private final Database database;

    public GetTurnstileEvents(final ServerConfig eventsConfig, final Database database) {
        this.eventsConfig = eventsConfig;
        this.database = database;
    }


    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final OutputStream outputStream = exchange.getResponseBody();
        int returnCode;
        String response;
        try {
            final Map<String, String> queryParameters = ServerUtils.getMapQuery(exchange, List.of("membership_id", "password"));
            if (!queryParameters.get("password").equals(eventsConfig.getPassword())) {
                throw new EventsException("Password is incorrect");
            }
            int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            try {
                final Membership membership = database.getMembership(membershipId);
                final List<TurnstileEvent> events = database.getTurnstileEvents(membershipId);
                final StringBuilder responseBuilder = new StringBuilder();
                responseBuilder.append("Info for membership with id = ").append(membership.getId()).append(System.lineSeparator());
                responseBuilder.append("Created at ").append(membership.getAddedTimeInSeconds()).append(System.lineSeparator());
                for (final TurnstileEvent event : events) {
                    responseBuilder.append(event.getEventId()).append(")")
                            .append(" time: ").append(event.getAddedTimeInSeconds()).append(",")
                            .append(" event ").append(event.getEvent()).append(System.lineSeparator());
                }
                response = responseBuilder.toString();
            } catch (final Exception e) {
                response = e.getMessage();
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
