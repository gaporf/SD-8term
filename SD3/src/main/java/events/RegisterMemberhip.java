package events;

import clock.SystemClock;
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

public class RegisterMemberhip implements HttpHandler {
    private final ServerConfig eventsConfig;
    private final ServerConfig reportConfig;
    private final Database database;

    public RegisterMemberhip(final ServerConfig eventsConfig, final ServerConfig reportConfig, final Database database) {
        this.eventsConfig = eventsConfig;
        this.reportConfig = reportConfig;
        this.database = database;
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final OutputStream outputStream = exchange.getResponseBody();
        int returnCode;
        String response;
        try {
            final Map<String, String> queryParameters = ServerUtils.getMapQuery(exchange, List.of("membership_id, membership_name, password"));
            if (!eventsConfig.getPassword().equals(queryParameters.get("password"))) {
                throw new EventsException("Password is incorrect");
            }
            final int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            final String membershipName = queryParameters.get("membership_name");
            final Membership membership = new Membership(membershipId, membershipName, new SystemClock().now().getEpochSecond());
            try {
                database.addMembership(membership);
                sendDataToReportServer(membership);
                response = "Membership with id = " + membershipId + " is added";
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

    private void sendDataToReportServer(final Membership membership) {
        final String result = ServerUtils.readAsText("http://localhost" + reportConfig.getPort() + "/" +
                "add_membership" + "?" +
                "password=" + reportConfig.getPassword() + "&" +
                "membership_id=" + membership.getId() + "&" +
                "membership_name=" + membership.getName());
        if (!result.equals("ok")) {
            throw new EventsException("Can't send new membership info to report server");
        }
    }
}
