package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import server.ServerConfig;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MasterActor extends AbstractActor {
    final private ActorConfig actorConfig;
    final private List<ServerConfig> servers;

    public MasterActor(final ActorConfig actorConfig, final List<ServerConfig> servers) {
        this.actorConfig = actorConfig;
        this.servers = servers;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RequestMessage.class, msg -> sender().tell(generateResponse(msg.getRequest()), self()))
                .build();
    }

    private String generateResponse(final String request) {
        final StringBuilder response = new StringBuilder();
        final List<ActorRef> children = new ArrayList<>();
        for (ServerConfig serverConfig : servers) {
            children.add(actorConfig.getActorSystem().actorOf(Props.create(ChildActor.class, serverConfig)));
        }
        final Timeout timeout = Timeout.create(Duration.ofSeconds(1));
        final List<Future<Object>> futures = new ArrayList<>();
        for (ActorRef child : children) {
            futures.add(Patterns.ask(child, new RequestMessage(request), timeout));
        }
        for (Future<Object> future : futures) {
            try {
                response.append((String) Await.result(future, timeout.duration())).append(System.lineSeparator());
            } catch (final Exception ignored) {
            }
        }
        for (ActorRef child : children) {
            actorConfig.getActorSystem().stop(child);
        }
        return response.toString();
    }
}
