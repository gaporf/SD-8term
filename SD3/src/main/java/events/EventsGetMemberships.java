package events;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.DataBaseMembership;
import database.DataBaseMembershipEvent;
import database.SqlDataBase;
import manager.ManagerException;
import server.ServerConfig;
import server.ServerUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class EventsGetMemberships implements HttpHandler {
    private final ServerConfig eventsConfig;
    private final SqlDataBase database;

    public EventsGetMemberships(final ServerConfig eventsConfig, final SqlDataBase database) {
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
            if (queryParameters.keySet().size() != 1 || !queryParameters.containsKey("password")) {
                throw new ManagerException("Requested pattern is /get_memberships?password=<password>");
            }
            if (!eventsConfig.getPassword().equals(queryParameters.get("password"))) {
                throw new EventsException("Password is incorrect");
            }
            try {
                final List<DataBaseMembership> memberships = database.getMemberships();
                final StringBuilder responseBuilder = new StringBuilder();
                responseBuilder.append("Info for memberships").append(System.lineSeparator());
                for (final DataBaseMembership membership : memberships) {
                    responseBuilder.append("Membership with id = ").append(membership.getId()).append(" with name ").append(membership.getName()).append(" created at ").append(membership.getAddedTime()).append(System.lineSeparator());
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
