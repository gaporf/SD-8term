package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.List;

public class FewResultsTestHandler implements HttpHandler {
    private final ServerConfig serverConfig;
    private final int results;

    public FewResultsTestHandler(final ServerConfig serverConfig, final int results) {
        this.serverConfig = serverConfig;
        this.results = results;
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final OutputStream outputStream = exchange.getResponseBody();
        final List<String> urls = new ArrayList<>();
        for (int i = 1; i <= results; i++) {
            urls.add("yandex" + i + ".ru");
        }
        final String response;
        switch (serverConfig.getResponseFormat()) {
            case JSON -> response = ServerUtils.getJsonResponse(new ServerResponse("test", "test", urls));
            case XML -> response = ServerUtils.getXmlResponse(new ServerResponse("test", "test", urls));
            default -> throw new ServerException("Unknown response format");
        }
        exchange.sendResponseHeaders(200, response.length());
        outputStream.write(response.getBytes(StandardCharsets.UTF_8));
        outputStream.close();
    }
}
