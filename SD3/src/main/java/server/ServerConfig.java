package server;

import clock.Clock;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ServerConfig {
    private final Clock clock;
    private final int port;
    private final String password;

    public ServerConfig(final String path, final Clock clock) {
        this.clock = clock;
        try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(Path.of(path).toFile())))) {
            final Map<String, String> parameters = new HashMap<>();
            for (final String line: bufferedReader.lines().collect(Collectors.toList())) {
                final String[] keyValue = line.split("=");
                if (keyValue.length != 2) {
                    throw new ServerUtilsException("Incorrect config file, every line should have <key>=<value> pattern, found " + line);
                }
                final String key = keyValue[0];
                final String value = keyValue[1];
                if (!(key.equals("port") || key.equals("password"))) {
                    throw new ServerUtilsException("Unknown key in config file, found " + key);
                }
                if (parameters.containsKey(key)) {
                    throw new ServerUtilsException("Every parameter should be exactly one, but found several lines for " + key);
                }
                parameters.put(key, value);
            }
            if (!parameters.containsKey("port")) {
                throw new ServerUtilsException("Can't find port for server");
            } else {
                try {
                    this.port = Integer.parseInt(parameters.get("port"));
                    if (this.port <= 0 || this.port > 65535) {
                        throw new ServerUtilsException("Port should be in range [1..65535]");
                    }
                } catch (final Exception e) {
                    throw new ServerUtilsException("Incorrect port", e);
                }
            }
            if (!parameters.containsKey("password")) {
                throw new ServerUtilsException("Can't find password for admin access");
            } else {
                this.password = parameters.get("password");
            }
        } catch (final Exception e) {
            throw new ServerUtilsException("Can't read file: " + e.getMessage(), e);
        }
    }

    public ServerConfig(final int port, final String password, final Clock clock) {
        this.port = port;
        this.password = password;
        this.clock = clock;
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public Clock getClock() {
        return clock;
    }
}
