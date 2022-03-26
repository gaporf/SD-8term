package turnstile;

import clock.SystemClock;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.ServerConfig;
import server.ServerUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class TurnstileEnter implements HttpHandler {
    private final ServerConfig turnstileConfig;
    private final ServerConfig eventsConfig;

    public TurnstileEnter(final ServerConfig turnstileConfig, final ServerConfig eventsConfig) {
        this.turnstileConfig = turnstileConfig;
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
                if (isMembershipValid(membershipId)) {
                    response = "Membership: id = " + membershipId + " passed";
                    final int eventId = TurnstileUtils.getLastEventId(membershipId, eventsConfig);
                    TurnstileUtils.sendDataToEventsServer(membershipId, eventId, "enter", eventsConfig);
                } else {
                    throw new TurnstileException("The membership is expired");
                }
            } catch (final Exception e) {
                response = "Can't enter: " + e.getMessage();
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

    private boolean isMembershipValid(final int membershipId) {
        final String membershipEvents = ServerUtils.readAsText("http://localhost:" + eventsConfig.getPort() + "/" +
                "get_membership_events" + "?" +
                "password=" + eventsConfig.getPassword() + "&" +
                "membership_id=" + membershipId);
        final String[] membershipEventsLines = membershipEvents.split(System.lineSeparator());
        if (!membershipEventsLines[0].equals("Info for membership id = " + membershipId)) {
            throw new TurnstileException(membershipEvents);
        } else if (membershipEventsLines.length == 2) {
            throw new TurnstileException("The membership is not activated");
        } else {
            final int validTill = Integer.parseInt(membershipEventsLines[membershipEventsLines.length - 1].split(", ")[1].split(" ")[2]);
            final long currentTime = turnstileConfig.getClock().now().getEpochSecond();
            return validTill > currentTime;
        }
    }
}
