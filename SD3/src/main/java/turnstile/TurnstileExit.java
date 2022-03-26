package turnstile;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.ServerConfig;
import server.ServerUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class TurnstileExit implements HttpHandler {
    private final ServerConfig eventsConfig;

    public TurnstileExit(final ServerConfig eventsConfig) {
        this.eventsConfig = eventsConfig;
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final OutputStream outputStream = exchange.getResponseBody();
        int returnCode;
        String response;
        try {
            final Map<String, String> queryParameters = ServerUtils.getMapQuery(exchange, List.of("membership_id"));
            final int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            try {
                final int eventId = TurnstileUtils.getLastEventId(membershipId, eventsConfig);
                response = "Membership: id = " + membershipId + " exited";
                TurnstileUtils.sendDataToEventsServer(membershipId, eventId, "exit", eventsConfig);
            } catch (final Exception e) {
                response = "Can't exit: " + e.getMessage();
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
