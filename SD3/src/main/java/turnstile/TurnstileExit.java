package turnstile;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.ManagerException;
import server.ServerConfig;
import server.ServerUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class TurnstileExit implements HttpHandler {
    private final ServerConfig eventsConfig;

    public TurnstileExit(final ServerConfig eventsConfig) {
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
            if (queryParameters.keySet().size() != 1 || !queryParameters.containsKey("membership_id")) {
                throw new ManagerException("Requested pattern is /exit?membership_id=<membership_id>");
            }
            int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            final String turnstileEvents = ServerUtils.readAsText("http://localhost:" + eventsConfig.getPort() + "/get_turnstile_events?password=" + eventsConfig.getPassword() + "&membership_id=" + membershipId);
            final String[] turnstileEventsLines = turnstileEvents.split(System.lineSeparator());
            if (!turnstileEventsLines[0].equals("Info for membership with id = " + membershipId)) {
                throw new TurnstileException(turnstileEvents);
            } else {
                final int eventId = turnstileEventsLines.length - 1;
                response = "Membership with id = " + membershipId + " exited";
                ServerUtils.readAsText("http://localhost:" + eventsConfig.getPort() + "/new_turnstile_event?password=" + eventsConfig.getPassword() + "&membership_id=" + membershipId + "&event_id=" + eventId + "&turnstile_event=exit");
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
