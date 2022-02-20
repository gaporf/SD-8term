package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ServerHandler implements HttpHandler {
    private final int timeoutInMilliseconds;
    private final ResponseFormat responseFormat;
    private final String serverName;
    private final List<String> websites;

    public ServerHandler(final String serverName, final ResponseFormat responseFormat, final int timeoutInMilliseconds) {
        this.serverName = serverName;
        this.responseFormat = responseFormat;
        this.timeoutInMilliseconds = timeoutInMilliseconds;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(
                        Path.of("C:\\Users\\gapor\\ITMO\\SD_2term\\SD1\\src\\main\\resources\\websites").toFile()
                )))) {
            websites = bufferedReader.lines().collect(Collectors.toList());
        } catch (final Exception e) {
            throw new ServerSearchException("Can't read websites", e);
        }
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final OutputStream outputStream = exchange.getResponseBody();
        final String quiresString = exchange.getRequestURI().getQuery();
        final String[] quires = quiresString == null ? null : quiresString.split("&");
        final String[] queryQuery = quires == null ? null : quires[0].split("=");
        if (quires == null || quires.length != 1 || queryQuery.length != 2 || !queryQuery[0].equals("query")) {
            final String response = "Bad request, format query=<query>";
            exchange.sendResponseHeaders(400, response.length());
            outputStream.write(response.getBytes(StandardCharsets.UTF_8));
        } else {
            if (timeoutInMilliseconds != 0) {
                try {
                    for (int i = 0; i < timeoutInMilliseconds; i += 5_000) {
                        Thread.sleep(Math.min(5_000, timeoutInMilliseconds - i));
                    }
                } catch (final Exception e) {
                    throw new ServerSearchException("Can't sleep", e);
                }
            }
            Collections.shuffle(websites);
            final List<String> foundWebsites = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                foundWebsites.add(websites.get(i));
            }
            final ServerResponse serverResponse = new ServerResponse(serverName, queryQuery[1], foundWebsites);
            final String response;
            switch (responseFormat) {
                case XML -> response = ServerUtils.getXmlResponse(serverResponse);
                case JSON -> response = ServerUtils.getJsonResponse(serverResponse);
                default -> throw new ServerSearchException("Unknown format");
            }
            exchange.sendResponseHeaders(200, response.length());
            outputStream.write(response.getBytes(StandardCharsets.UTF_8));
        }
        outputStream.close();
    }
}
