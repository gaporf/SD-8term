package events;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.*;
import manager.ManagerException;
import server.ServerConfig;
import server.ServerUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class EventsGetTurnstileEvents implements HttpHandler {
    private final ServerConfig eventsConfig;
    private final SqlDataBase database;

    public EventsGetTurnstileEvents(final ServerConfig eventsConfig, final SqlDataBase database) {
        this.eventsConfig = eventsConfig;
        this.database = database;
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final OutputStream outputStream = exchange.getResponseBody();
        final String queryString = exchange.getRequestURI().getQuery();
        int returnCode;
        String response;
        final Map<String, String> queryParameters;
        try {
            queryParameters = ServerUtils.getMapQuery(queryString);
            if (queryParameters.keySet().size() != 2 ||
                    !queryParameters.containsKey("membership_id") || !queryParameters.containsKey("password")) {
                throw new ManagerException("Requested pattern is /get_turnstile_event?membership_id=<membership_id>&password=<password>");
            }
            if (!queryParameters.get("password").equals(eventsConfig.getPassword())) {
                throw new EventsException("Password is incorrect");
            }
            int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            try {
                final DataBaseMembership membership = database.getMembership(membershipId);
                final List<DataBaseTurnstileEvent> events = database.getTurnstileEvents(membershipId);
                final StringBuilder responseBuilder = new StringBuilder();
                responseBuilder.append("Info for membership with id = ").append(membership.getId()).append(System.lineSeparator());
                responseBuilder.append("Created at ").append(membership.getAddedTime()).append(System.lineSeparator());
                for (final DataBaseTurnstileEvent event : events) {
                    responseBuilder.append(event.getEventId()).append(") time: ").append(event.getAddedTime()).append(", event ").append(event.getEvent()).append(System.lineSeparator());
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
