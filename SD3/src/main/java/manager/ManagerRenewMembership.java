package manager;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.ServerConfig;
import server.ServerUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ManagerRenewMembership implements HttpHandler {
    private final ServerConfig eventsConfig;

    public ManagerRenewMembership(final ServerConfig eventsConfig) {
        this.eventsConfig = eventsConfig;
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
                    !queryParameters.containsKey("membership_id") || !queryParameters.containsKey("event_id") ||
                    !queryParameters.containsKey("valid_till")) {
                throw new ManagerException("Requested pattern is /renew_membership?membership_id=<membership_id>&event_id=<event_id>&valid_till=<valid_till>");
            }
            int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            int eventId = ServerUtils.parseInt(queryParameters.get("event_id"));
            int validTill = ServerUtils.parseInt(queryParameters.get("valid_till"));
            final String result = ServerUtils.readAsText("http://localhost:" + eventsConfig.getPort() + "/renew_membership?password=" + eventsConfig.getPassword() + "&membership_id=" + membershipId + "&event_id=" + eventId + "&valid_till=" + validTill);
            if (!result.equals("MembershipEvent for membership with id = " + membershipId + " and with event id = " + eventId + " was added")) {
                throw new ManagerException(result);
            }
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
}
