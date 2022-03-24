package events;

import clock.SystemClock;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.TurnstileEvent;
import database.Database;
import database.TypeOfTurnstileEvent;
import server.ServerConfig;
import server.ServerUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class NewTurnstileEvent implements HttpHandler {
    private final ServerConfig eventsConfig;
    private final ServerConfig reportConfig;
    private final Database database;

    public NewTurnstileEvent(final ServerConfig eventsConfig, final ServerConfig reportConfig, final Database database) {
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
            final Map<String, String> queryParameters = ServerUtils.getMapQuery(exchange,
                    List.of("membership_id", "event_id", "event", "password"));
            if (!queryParameters.get("password").equals(eventsConfig.getPassword())) {
                throw new EventsException("Password is incorrect");
            }
            int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            int eventId = ServerUtils.parseInt(queryParameters.get("event_id"));
            final String event = queryParameters.get("event");
            try {
                final TurnstileEvent turnstileEvent = new TurnstileEvent(eventId, membershipId,
                        TypeOfTurnstileEvent.valueOf(event.toUpperCase()), new SystemClock().now().getEpochSecond());
                database.addTurnstileEvent(turnstileEvent);
                sendDataToReportServer(turnstileEvent);
                response = "Turnstile event: id = " + eventId + " for membership: id = " + membershipId + " is added";
            } catch (final Exception e) {
                response = e.getMessage();
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

    private void sendDataToReportServer(final TurnstileEvent turnstileEvent) {
        final String result = ServerUtils.readAsText("http://localhost:" + reportConfig.getPort() + "/" +
                turnstileEvent.getEvent().toString().toLowerCase() + "?" +
                "password=" + reportConfig.getPassword() + "&" +
                "membership_id=" + turnstileEvent.getMembershipId() + "&" +
                "time_in_seconds=" + turnstileEvent.getAddedTimeInSeconds());
        if (!result.equals("ok")) {
            throw new EventsException("Can't send data to report server");
        }
    }
}
