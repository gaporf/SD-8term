package server;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.json.Json;
import javax.json.JsonObject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class ServerUtils {
    public static String getXmlResponse(final ServerResponse serverResponse) {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (final Exception e) {
            throw new ServerSearchException("Can't create XML Document", e);
        }
        final Document document = documentBuilder.newDocument();
        final Element rootElement = document.createElement("response");
        document.appendChild(rootElement);
        final Element serverNameElement = document.createElement("serverName");
        rootElement.appendChild(serverNameElement);
        serverNameElement.appendChild(document.createTextNode(serverResponse.getServerName()));
        final Element queryElement = document.createElement("query");
        rootElement.appendChild(queryElement);
        queryElement.appendChild(document.createTextNode(serverResponse.getRequest()));
        final Element urlsElement = document.createElement("urls");
        rootElement.appendChild(urlsElement);
        final Element urlsNumElement = document.createElement("num");
        urlsElement.appendChild(urlsNumElement);
        urlsNumElement.appendChild(document.createTextNode(Integer.toString(serverResponse.getUrls().size())));
        final Element urlsListElement = document.createElement("list");
        urlsElement.appendChild(urlsListElement);
        for (String url : serverResponse.getUrls()) {
            final Element urlsListIthElement = document.createElement("url");
            urlsListElement.appendChild(urlsListIthElement);
            urlsListIthElement.appendChild(document.createTextNode(url));
        }
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (final Exception e) {
            throw new ServerSearchException("Can't create XML Transformer", e);
        }
        final DOMSource domSource = new DOMSource(document);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final StreamResult streamResult = new StreamResult(byteArrayOutputStream);
        try {
            transformer.transform(domSource, streamResult);
        } catch (final Exception e) {
            throw new ServerSearchException("Can't send XML to client", e);
        }
        return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
    }

    public static String getJsonResponse(final ServerResponse serverResponse) {
        final JsonObject urlsJson = Json.createObjectBuilder()
                .add("num", serverResponse.getUrls().size())
                .add("list", Json.createArrayBuilder(serverResponse.getUrls()))
                .build();
        return Json.createObjectBuilder()
                .add("serverName", serverResponse.getServerName())
                .add("query", serverResponse.getRequest())
                .add("urls", urlsJson).build().toString();
    }
}
