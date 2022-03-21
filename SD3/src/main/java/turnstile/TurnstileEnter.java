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

public class TurnstileEnter implements HttpHandler {
    private final ServerConfig eventsConfig;

    public TurnstileEnter(final ServerConfig eventsConfig) {
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
                throw new ManagerException("Requested pattern is /enter?membership_id=<membership_id>");
            }
            int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            final String membershipEvents = ServerUtils.readAsText("http://localhost:" + eventsConfig.getPort() + "/get_membership_events?password=" + eventsConfig.getPassword() + "&membership_id=" + membershipId);
            final String turnstileEvents = ServerUtils.readAsText("http://localhost:" + eventsConfig.getPort() + "/get_turnstile_events?password=" + eventsConfig.getPassword() + "&membership_id=" + membershipId);
            final String[] membershipEventsLines = membershipEvents.split(System.lineSeparator());
            final String[] turnstileEventsLines = turnstileEvents.split(System.lineSeparator());
            if (!membershipEventsLines[0].equals("Info for membership with id = " + membershipId)) {
                throw new TurnstileException(membershipEvents);
            } else if (membershipEventsLines.length == 2) {
                throw new TurnstileException("The membership is not activated");
            } else {
                final int eventId = turnstileEventsLines.length - 1;
                final int validTill = Integer.parseInt(membershipEventsLines[membershipEventsLines.length - 1].split(", ")[1].split(" ")[2]);
                final int currentTime = (int) (System.currentTimeMillis() / 1000);
                if (validTill >= currentTime) {
                    response = "Membership with id = " + membershipId + " passed";
                    final String res = ServerUtils.readAsText("http://localhost:" + eventsConfig.getPort() + "/new_turnstile_event?password=" + eventsConfig.getPassword() + "&membership_id=" + membershipId + "&event_id=" + eventId + "&turnstile_event=enter");
                    System.out.println(res);
                } else {
                    throw new TurnstileException("The membership is expired");
                }
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
