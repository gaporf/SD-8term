package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.rmi.ServerException;
import java.util.List;

public class EmptyResultTestHandler implements HttpHandler {
    final ServerConfig serverConfig;

    public EmptyResultTestHandler(final ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final OutputStream outputStream = exchange.getResponseBody();
        final String response;
        switch (serverConfig.getResponseFormat()) {
            case JSON -> response = ServerUtils.getJsonResponse(new ServerResponse("test", "test", List.of()));
            case XML -> response = ServerUtils.getXmlResponse(new ServerResponse("test", "test", List.of()));
            default -> throw new ServerException("Unknown response format");
        }
        exchange.sendResponseHeaders(200, response.length());
        outputStream.write(response.getBytes(StandardCharsets.UTF_8));
        outputStream.close();
    }
}
