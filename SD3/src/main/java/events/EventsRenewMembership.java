package events;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.DataBaseMembershipEvent;
import database.SqlDataBase;
import manager.ManagerException;
import server.ServerConfig;
import server.ServerUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class EventsRenewMembership implements HttpHandler {
    private final ServerConfig eventsConfig;
    private final SqlDataBase database;

    public EventsRenewMembership(final ServerConfig eventsConfig, final SqlDataBase database) {
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
            if (queryParameters.keySet().size() != 4 ||
                    !queryParameters.containsKey("membership_id") || !queryParameters.containsKey("event_id") ||
                    !queryParameters.containsKey("valid_till") || !queryParameters.containsKey("password")) {
                throw new ManagerException("Requested pattern is /renew_membership?membership_id=<membership_id>&event_id=<event_id>&valid_till=<valid_till>&password=<password>");
            }
            if (!eventsConfig.getPassword().equals(queryParameters.get("password"))) {
                throw new EventsException("Password is incorrect");
            }
            final int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            final int eventId = ServerUtils.parseInt(queryParameters.get("event_id"));
            final int valid_till = ServerUtils.parseInt(queryParameters.get("valid_till"));
            try {
                database.addMembershipEvent(new DataBaseMembershipEvent(eventId, membershipId, valid_till));
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
