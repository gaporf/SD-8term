package events;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.DataBaseMembership;
import database.SqlDataBase;
import manager.ManagerException;
import server.ServerConfig;
import server.ServerUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class EventsRegisterMembership implements HttpHandler {
    private final ServerConfig eventsConfig;
    private final ServerConfig reportConfig;
    private final SqlDataBase database;

    public EventsRegisterMembership(final ServerConfig eventsConfig, final ServerConfig reportConfig, final SqlDataBase database) {
        this.eventsConfig = eventsConfig;
        this.reportConfig = reportConfig;
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
            if (queryParameters.keySet().size() != 3 ||
                    !queryParameters.containsKey("membership_id") || !queryParameters.containsKey("membership_name") ||
                    !queryParameters.containsKey("password")) {
                throw new ManagerException("Requested pattern is /register_membership?membership_id=<membership_id>&membership_name=<membership_name>&password=<password>");
            }
            if (!eventsConfig.getPassword().equals(queryParameters.get("password"))) {
                throw new EventsException("Password is incorrect");
            }
            final int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            final String membershipName = queryParameters.get("membership_name");
            try {
                database.addMembership(new DataBaseMembership(membershipId, membershipName));
                ServerUtils.readAsText("http://localhost:" + reportConfig.getPort() + "/add_membership?password=" + reportConfig.getPassword() + "&membership_id=" + membershipId + "&name=" + membershipName);
                response = "Membership with id = " + membershipId + " was added";
            } catch (final Exception e) {
                response = "Can't add membership: " + e.getMessage();
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
