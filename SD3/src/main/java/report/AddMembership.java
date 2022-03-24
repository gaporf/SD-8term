package report;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.ServerConfig;
import server.ServerUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class AddMembership implements HttpHandler {
    private final ServerConfig reportConfig;
    private final ReportLocalStorage reportLocalStorage;

    public AddMembership(final ServerConfig reportConfig, final ReportLocalStorage reportLocalStorage) {
        this.reportConfig = reportConfig;
        this.reportLocalStorage = reportLocalStorage;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        final OutputStream outputStream = exchange.getResponseBody();
        int returnCode;
        String response;
        try {
            final Map<String, String> queryParameters = ServerUtils.getMapQuery(exchange, List.of("password", "membership_id", "membership_name"));
            final String password = queryParameters.get("password");
            if (!password.equals(reportConfig.getPassword())) {
                throw new ReportException("Password is incorrect");
            }
            final int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            final String name = queryParameters.get("membership_name");
            reportLocalStorage.addMembership(membershipId, name);
            response = "ok";
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
