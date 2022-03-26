package report;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.ServerUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ReportStatistics implements HttpHandler {
    private final ReportLocalStorage reportLocalStorage;

    public ReportStatistics(final ReportLocalStorage reportLocalStorage) {
        this.reportLocalStorage = reportLocalStorage;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final OutputStream outputStream = exchange.getResponseBody();
        int returnCode;
        String response;
        try {
            final Map<String, String> queryParameters = ServerUtils.getMapQuery(exchange, List.of("membership_id"));
            int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            try {
                response = reportLocalStorage.getStatistics(membershipId);
            } catch (final Exception e) {
                response = "Can't get statistics: " + e.getMessage();
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
