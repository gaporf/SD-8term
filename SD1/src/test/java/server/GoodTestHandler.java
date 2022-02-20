package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.rmi.ServerException;
import java.util.List;

public class GoodTestHandler implements HttpHandler {
    final private ServerConfig serverConfig;

    public GoodTestHandler(final ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final OutputStream outputStream = exchange.getResponseBody();
        final ServerResponse serverResponse = new ServerResponse(
                "test",
                "test",
                List.of(
                        "yandex.ru", "google.com", "bing.com",
                        "mail.ru", "yahoo.com", "microsoft.com",
                        "twitch.tv", "youtube.com", "spotify.com", "ozon.ru"
                ));
        final String response;
        switch (serverConfig.getResponseFormat()) {
            case JSON -> response = ServerUtils.getJsonResponse(serverResponse);
            case XML -> response = ServerUtils.getXmlResponse(serverResponse);
            default -> throw new ServerException("Unknown response format");
        }
        if (serverConfig.getTimeoutInMilliseconds() != 0) {
            for (int i = 0; i < serverConfig.getTimeoutInMilliseconds(); i += 5_000) {
                try {
                    Thread.sleep(Math.min(5_000, serverConfig.getTimeoutInMilliseconds()) - i);
                } catch (final Exception ignored) {
                }
            }
        }
        exchange.sendResponseHeaders(200, response.length());
        outputStream.write(response.getBytes(StandardCharsets.UTF_8));
        outputStream.close();
    }
}
