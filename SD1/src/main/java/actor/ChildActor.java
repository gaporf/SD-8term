package actor;

import akka.actor.AbstractActor;
import server.ServerConfig;

import java.util.List;

public class ChildActor extends AbstractActor {
    final ServerConfig server;

    public ChildActor(final ServerConfig server) {
        this.server = server;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RequestMessage.class, msg -> sender().tell(generateResponse(msg), self()))
                .build();
    }

    private String generateResponse(final RequestMessage msg) {
        final StringBuilder response = new StringBuilder();
        List<String> websites;
        try {
            websites = getWebsites(msg.getRequest());
            response.append(server.getServerName()).append(System.lineSeparator());
        } catch (final Exception e) {
            websites = List.of();
        }
        for (String website : websites) {
            response.append("    ").append(website).append(System.lineSeparator());
        }
        return response.toString();
    }

    private List<String> getWebsites(final String request) {
        final String response = ActorUtils.readAsText("http://localhost:" + server.getPort() + server.getPath() + "?query=" + request);
        final List<String> result;
        switch (server.getResponseFormat()) {
            case JSON -> result = ActorUtils.parseJson(response);
            case XML -> result = ActorUtils.parseXml(response);
            default -> throw new ActorException("Unknown response format");
        }
        return result;
    }
}
