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

public class MembershipInfo implements HttpHandler {
    private final ServerConfig eventsConfig;

    public MembershipInfo(final ServerConfig eventsConfig) {
        this.eventsConfig = eventsConfig;
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final OutputStream outputStream = exchange.getResponseBody();
        int returnCode;
        String response;
        try {
            final Map<String, String> queryParameters = ServerUtils.getMapQuery(exchange, List.of("membership_id"));
            int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            try {
                response = getDataFromEventsServer(membershipId);
            } catch (final Exception e) {
                response = "Can't get membership info: " + e.getMessage();
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

    private String getDataFromEventsServer(final int membershipId) {
        final String result = ServerUtils.readAsText("http://localhost:" + eventsConfig.getPort() + "/" +
                "get_membership_events" + "?" +
                "password=" + eventsConfig.getPassword() + "&" +
                "membership_id=" + membershipId);
        final String[] lines = result.split(System.lineSeparator());
        if (!lines[0].equals("Info for membership id = " + membershipId)) {
            throw new ManagerException(result);
        } else {
            final String validTillInfo = (lines.length == 2 ? "The membership is not activated" : lines[lines.length - 1].split(", ")[1]);
            return lines[0] + System.lineSeparator() +
                    lines[1] + System.lineSeparator() +
                    validTillInfo;
        }
    }
}
