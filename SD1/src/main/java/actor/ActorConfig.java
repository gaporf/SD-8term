package actor;

import akka.actor.ActorSystem;

public class ActorConfig {
    final private ActorSystem actorSystem;

    public ActorConfig(final ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    public ActorSystem getActorSystem() {
        return actorSystem;
    }
}
