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

public class ReportAddMembership implements HttpHandler {
    private final ServerConfig reportConfig;
    private final ReportLocalStorage reportLocalStorage;

    public ReportAddMembership(final ServerConfig reportConfig, final ReportLocalStorage reportLocalStorage) {
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
                    !queryParameters.containsKey("membership_id") || !queryParameters.containsKey("name")) {
                throw new ManagerException("Requested pattern is /add_membership?password=<password>&membership_id=<membership_id>&name=<name>");
            }
            final String password = queryParameters.get("password");
            if (!password.equals(reportConfig.getPassword())) {
                throw new ReportException("Password is incorrect");
            }
            final int membershipId = ServerUtils.parseInt(queryParameters.get("membership_id"));
            final String name = queryParameters.get("name");
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
