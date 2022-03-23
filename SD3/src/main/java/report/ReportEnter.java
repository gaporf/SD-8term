package report;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.ManagerException;
import server.ServerConfig;
import server.ServerUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ReportEnter implements HttpHandler {
    private final ServerConfig reportConfig;
    private final ReportLocalStorage reportLocalStorage;

    public ReportEnter(final ServerConfig reportConfig, final ReportLocalStorage reportLocalStorage) {
        this.reportConfig = reportConfig;
        this.reportLocalStorage = reportLocalStorage;
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
            if (queryParameters.keySet().size() != 3 || !queryParameters.containsKey("password") ||
                    !queryParameters.containsKey("membership_id") || !queryParameters.containsKey("time_in_seconds")) {
                throw new ManagerException("Requested pattern is /enter?password=<password>&membership_id=<membership_id>&time_in_seconds=<time_in_seconds>");
            }
            final String password = queryParameters.get("password");
            if (!password.equals(reportConfig.getPassword())) {
                throw new ReportException("Password is incorrect");
            }
            final int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            final int timeInSeconds = ServerUtils.parseInt(queryParameters.get("time_in_seconds"));
            reportLocalStorage.enter(membershipId, timeInSeconds);
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
