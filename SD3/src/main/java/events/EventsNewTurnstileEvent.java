package events;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import database.DataBaseTurnstileEvent;
import database.SqlDataBase;
import database.TurnstileEvent;
import manager.ManagerException;
import server.ServerConfig;
import server.ServerUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class EventsNewTurnstileEvent implements HttpHandler {
    private final ServerConfig eventsConfig;
    private final ServerConfig reportConfig;
    private final SqlDataBase database;

    public EventsNewTurnstileEvent(final ServerConfig eventsConfig, final ServerConfig reportConfig, final SqlDataBase database) {
        this.eventsConfig = eventsConfig;
        this.reportConfig = reportConfig;
        this.database = database;
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
            if (queryParameters.keySet().size() != 4 ||
                    !queryParameters.containsKey("membership_id") || !queryParameters.containsKey("event_id") ||
                    !queryParameters.containsKey("turnstile_event") || !queryParameters.containsKey("password")) {
                throw new ManagerException("Requested pattern is /new_turnstile_event?password=<password>&membership_id=<membership_id>&event_id=<event_id>&turnstile_event=<turnstile_event>");
            }
            if (!queryParameters.get("password").equals(eventsConfig.getPassword())) {
                throw new EventsException("Password is incorrect");
            }
            int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            int eventId = ServerUtils.parseInt(queryParameters.get("event_id"));
            final String turnstileEvent = queryParameters.get("turnstile_event");
            try {
                final int curTime = (int) (System.currentTimeMillis() / 1000);
                database.addTurnstileEvent(new DataBaseTurnstileEvent(eventId, membershipId, TurnstileEvent.valueOf(turnstileEvent.toUpperCase()), curTime));
                ServerUtils.readAsText("http://localhost:" + reportConfig.getPort() + "/" + turnstileEvent.toLowerCase() + "?password=" + reportConfig.getPassword() + "&membership_id=" + membershipId + "&time_in_seconds=" + curTime);
                response = "Turnstile event with id = " + eventId + " for membership with id = " + membershipId + " was added";
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
}
