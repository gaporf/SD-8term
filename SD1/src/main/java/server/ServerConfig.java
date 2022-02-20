package server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ServerConfig {
    private final String serverName;
    private final int port;
    private final String path;
    private final ResponseFormat responseFormat;
    private final int timeoutInMilliseconds;

    public ServerConfig(final String serverName, final int port, final String path, final ResponseFormat responseFormat, final int timeoutInMilliseconds) {
        this.serverName = serverName;
        this.port = port;
        this.path = path;
        this.responseFormat = responseFormat;
        this.timeoutInMilliseconds = timeoutInMilliseconds;
    }

    public ServerConfig(final String path) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(Path.of(path).toFile())))) {
            final Map<String, String> parameters = new HashMap<>();
            for (final String line : bufferedReader.lines().collect(Collectors.toList())) {
                final String[] keyValue = line.split("=");
                if (keyValue.length != 2) {
                    throw new ServerSearchException("Incorrect config file, every line should have <key>=<value> pattern, found " + line);
                }
                final String key = keyValue[0];
                final String value = keyValue[1];
                if (!(key.equals("serverName") || key.equals("port") || key.equals("path") || key.equals("responseFormat") || key.equals("timeout"))) {
                    throw new ServerSearchException("Unknown key in config file, found " + key);
                }
                if (parameters.containsKey(key)) {
                    throw new ServerSearchException("Every parameter should be exactly one, but found several lines for " + key);
                }
                parameters.put(key, value);
            }
            if (!parameters.containsKey("serverName")) {
                throw new ServerSearchException("Can't find serverName");
            } else {
                this.serverName = parameters.get("serverName");
            }
            if (!parameters.containsKey("port")) {
                throw new ServerSearchException("Can't find port");
            } else {
                try {
                    this.port = Integer.parseInt(parameters.get("port"));
                    if (this.port <= 0 || this.port > 65535) {
                        throw new ServerSearchException("Port number should be in range [1, 65535]");
                    }
                } catch (final Exception e) {
                    throw new ServerSearchException("Port number is invalid", e);
                }
            }
            if (!parameters.containsKey("path")) {
                throw new ServerSearchException("Can't find path");
            } else {
                this.path = parameters.get("path");
            }
            if (!parameters.containsKey("responseFormat")) {
                throw new ServerSearchException("Can't find responseFormat");
            } else {
                final String format = parameters.get("responseFormat");
                switch (format) {
                    case "XML" -> this.responseFormat = ResponseFormat.XML;
                    case "JSON" -> this.responseFormat = ResponseFormat.JSON;
                    default -> throw new ServerSearchException("Unknown format " + format);
                }
            }
            if (!parameters.containsKey("timeout")) {
                throw new ServerSearchException("Can't find timeout");
            } else {
                try {
                    this.timeoutInMilliseconds = Integer.parseInt(parameters.get("timeout"));
                    if (this.timeoutInMilliseconds < 0) {
                        throw new ServerSearchException("Incorrect timeout value");
                    }
                } catch (final Exception e) {
                    throw new ServerSearchException("Can't parse timeout value", e);
                }
            }
        } catch (final Exception e) {
            throw new ServerSearchException("Can't read file", e);
        }
    }

    public String getServerName() {
        return serverName;
    }

    public int getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public ResponseFormat getResponseFormat() {
        return responseFormat;
    }

    public int getTimeoutInMilliseconds() {
        return timeoutInMilliseconds;
    }
}
