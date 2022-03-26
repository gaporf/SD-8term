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

public class GiveMembership implements HttpHandler {
    private final ServerConfig eventsConfig;

    public GiveMembership(final ServerConfig eventsConfig) {
        this.eventsConfig = eventsConfig;
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final OutputStream outputStream = exchange.getResponseBody();
        int returnCode;
        String response;
        try {
            final Map<String, String> queryParameters = ServerUtils.getMapQuery(exchange, List.of("membership_id", "membership_name"));
            final int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            final String membershipName = queryParameters.get("membership_name");
            try {
                sendDataToEventsServer(membershipId, membershipName);
                response = "Membership is given: id = " + membershipId;
            } catch (final Exception e) {
                response = "Can't give membership: " + e.getMessage();
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

    private void sendDataToEventsServer(final int membershipId, final String membershipName) {
        final String result = ServerUtils.readAsText("http://localhost:" + eventsConfig.getPort() + "/" +
                "register_membership?password=" + eventsConfig.getPassword() + "&" +
                "membership_id=" + membershipId + "&" +
                "membership_name=" + membershipName);
        if (!result.equals("Membership: id = " + membershipId + " is added" + System.lineSeparator())) {
            throw new ManagerException(result);
        }
    }
}
