package events;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.Database;
import database.Membership;
import server.ServerConfig;
import server.ServerUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class GetMemberships implements HttpHandler {
    private final ServerConfig eventsConfig;
    private final Database database;

    public GetMemberships(final ServerConfig eventsConfig, final Database database) {
        this.eventsConfig = eventsConfig;
        this.database = database;
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final OutputStream outputStream = exchange.getResponseBody();
        int returnCode;
        String response;
        try {
            final Map<String, String> queryParameters = ServerUtils.getMapQuery(exchange, List.of("password"));
            if (!eventsConfig.getPassword().equals(queryParameters.get("password"))) {
                throw new EventsException("Password is incorrect");
            }
            try {
                final List<Membership> memberships = database.getMemberships();
                final StringBuilder responseBuilder = new StringBuilder();
                responseBuilder.append("Info for memberships").append(System.lineSeparator());
                for (final Membership membership : memberships) {
                    responseBuilder.append("Membership: id = ").append(membership.getId()).append(",")
                            .append(" name = ").append(membership.getName()).append(",")
                            .append(" created at ").append(membership.getAddedTimeInSeconds()).append(System.lineSeparator());
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
