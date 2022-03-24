package manager;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.ServerConfig;
import server.ServerUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class RenewMembership implements HttpHandler {
    private final ServerConfig eventsConfig;

    public RenewMembership(final ServerConfig eventsConfig) {
        this.eventsConfig = eventsConfig;
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final OutputStream outputStream = exchange.getResponseBody();
        int returnCode;
        String response;
        try {
            final Map<String, String> queryParameters = ServerUtils.getMapQuery(exchange, List.of("membership_id", "event_id", "valid_till"));
            int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            int eventId = ServerUtils.parseInt(queryParameters.get("event_id"));
            int validTill = ServerUtils.parseInt(queryParameters.get("valid_till"));
            sendDataToEventsServer(membershipId, eventId, validTill);
            response = "Membership was renewed, membership_id = " + membershipId + ", event_id = " + eventId + " valid till " + validTill;
            returnCode = 200;
        } catch (final Exception e) {
            response = e.getMessage();
            returnCode = 400;
        }
        exchange.sendResponseHeaders(returnCode, response.length());
        outputStream.write(response.getBytes(StandardCharsets.UTF_8));
        outputStream.close();
    }

    private void sendDataToEventsServer(final int membershipId, final int eventId, final int validTill) {
        final String result = ServerUtils.readAsText("http://localhost:" + eventsConfig.getPort() + "/" +
                "renew_membership" + "?" +
                "password=" + eventsConfig.getPassword() + "&" +
                "membership_id=" + membershipId + "&" +
                "event_id=" + eventId + "&" +
                "valid_till=" + validTill);
        if (!result.equals("MembershipEvent for membership with id = " + membershipId + " and with event id = " + eventId + " was added")) {
            throw new ManagerException(result);
        }
    }
}
