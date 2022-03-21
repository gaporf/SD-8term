package manager;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.ServerConfig;
import server.ServerUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ManagerMembershipInfo implements HttpHandler {
    private final ServerConfig eventsConfig;

    public ManagerMembershipInfo(final ServerConfig eventsConfig) {
        this.eventsConfig = eventsConfig;
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final OutputStream outputStream = exchange.getResponseBody();
        final String queryString = exchange.getRequestURI().getQuery();
        int returnCode;
        String response;
        final Map<String, String> queryParameters;
        try {
            queryParameters = ServerUtils.getMapQuery(queryString);
            if (queryParameters.keySet().size() != 1 || !queryParameters.containsKey("membership_id")) {
                throw new ManagerException("Requested pattern is /membership_info?membership_id=<membership_id>");
            }
            int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            final String result = ServerUtils.readAsText("http://localhost:" + eventsConfig.getPort() + "/get_membership_events?password=" + eventsConfig.getPassword() + "&membership_id=" + membershipId);
            final String[] lines = result.split(System.lineSeparator());
            if (!lines[0].equals("Info for membership with id = " + membershipId)) {
                throw new ManagerException(result);
            } else {
                response = lines[0] + System.lineSeparator() +
                        lines[1] + System.lineSeparator() +
                        lines[lines.length - 1].split(", ")[1];
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
