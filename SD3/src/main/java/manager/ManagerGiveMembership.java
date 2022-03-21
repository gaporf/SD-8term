package manager;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.ServerConfig;
import server.ServerUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ManagerGiveMembership implements HttpHandler {
    private final ServerConfig eventsConfig;

    public ManagerGiveMembership(final ServerConfig eventsConfig) {
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
            if (queryParameters.keySet().size() != 2 ||
                    !queryParameters.containsKey("membership_id") || !queryParameters.containsKey("membership_name")) {
                throw new ManagerException("Requested pattern is /give_membership?membership_id=<membership_id>&membership_name=<membership_name>");
            }
            final int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            final String membershipName = queryParameters.get("membership_name");
            final String result = ServerUtils.readAsText("http://localhost:" + eventsConfig.getPort() + "/register_membership?password=" + eventsConfig.getPassword() + "&membership_id=" + membershipId + "&membership_name=" + membershipName);
            if (!result.equals("Membership with id = " + membershipId + " was added")) {
                throw new ManagerException(result);
            }
            response = "Membership was given for id = " + membershipId;
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
