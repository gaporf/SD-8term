package server;

import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerUtils {
    private static Map<String, String> getMapQuery(final String queryString) {
        final Map<String, String> result = new HashMap<>();
        final String[] queries = queryString.split("&");
        for (final String query : queries) {
            final String[] pairQuery = query.split("=");
            if (pairQuery.length != 2) {
                throw new ServerUtilsException("Unexpected parameter string, expected <parameter>=<value>");
            }
            result.put(pairQuery[0], pairQuery[1]);
        }
        return result;
    }

    public static int parseInt(final String integer) {
        try {
            return Integer.parseInt(integer);
        } catch (final Exception e) {
            throw new ServerUtilsException("Can't parse int " + integer + ": " + e.getMessage(), e);
        }
    }

    public static String readAsText(final String sourceUrl) {
        final URL url;
        try {
            url = new URL(sourceUrl);
        } catch (final MalformedURLException e) {
            throw new ServerUtilsException("Malformed URL: " + e.getMessage(), e);
        }
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
            final StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append(System.lineSeparator());
            }
            return response.toString();
        } catch (final IOException e) {
            throw new ServerUtilsException("Can't access website: " + e.getMessage(), e);
        }
    }

    public static Map<String, String> getMapQuery(final HttpExchange exchange, final List<String> parameters) {
        final String queryString = exchange.getRequestURI().getQuery();
        final StringBuilder patternBuilder = new StringBuilder();
        for (final String parameter : parameters) {
            patternBuilder.append(parameter).append("&");
        }
        patternBuilder.delete(patternBuilder.length() - 1, patternBuilder.length());
        final String pattern = patternBuilder.toString();
        final Map<String, String> queryParameters;
        try {
            queryParameters = getMapQuery(queryString);
            if (queryParameters.keySet().size() != parameters.size()) {
                throw new ServerUtilsException("Expected pattern is " + pattern);
            }
            for (final String parameter : parameters) {
                if (!queryParameters.containsKey(parameter)) {
                    throw new ServerUtilsException("Expected pattern is " + pattern);
                }
            }

            return queryParameters;
        } catch (final Exception e) {
            throw new ServerUtilsException("Can't handle: " + e.getMessage(), e);
        }
    }
}
