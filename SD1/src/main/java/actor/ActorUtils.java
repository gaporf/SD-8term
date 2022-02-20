package actor;

import org.w3c.dom.*;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.stream.JsonParser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ActorUtils {
    public static String readAsText(final String sourceUrl) {
        final URL url;
        try {
            url = new URL(sourceUrl);
        } catch (final MalformedURLException e) {
            throw new ActorException("Malformed URL", e);
        }
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
            final StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine).append(System.lineSeparator());
            }
            return response.toString();
        } catch (final IOException e) {
            throw new ActorException("Can't access website", e);
        }
    }

    public static List<String> parseJson(final String jsonString) {
        final JsonParser jsonParser = Json.createParser(new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8)));
        for (int i = 0; i < 7; i++) {
            jsonParser.next();
        }
        final JsonArray jsonArray = jsonParser.getObject().getJsonArray("list");
        final List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(5, jsonArray.size()); i++) {
            result.add(jsonArray.getString(i));
        }
        return result;
    }

    public static List<String> parseXml(final String xmlString) {
        final List<String> result = new ArrayList<>();
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (final Exception e) {
            throw new ActorException("Can't create document builder", e);
        }
        final Document document;
        try {
            document = builder.parse(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)));
        } catch (final Exception e) {
            throw new ActorException("Can't parse xml string", e);
        }
        try {
            final Node urlsElement = document.getDocumentElement().getChildNodes().item(2);
            final NodeList urlsList = urlsElement.getChildNodes().item(1).getChildNodes();
            for (int i = 0; i < Math.min(5, urlsList.getLength()); i++) {
                result.add(urlsList.item(i).getChildNodes().item(0).getNodeValue());
            }
            return result;
        } catch (final Exception e) {
            throw new ActorException("Invalid xml string", e);
        }
    }
}
