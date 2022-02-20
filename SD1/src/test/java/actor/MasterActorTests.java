package actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import org.junit.Assert;
import org.junit.Test;
import scala.concurrent.Await;
import scala.concurrent.Future;
import server.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MasterActorTests {
    @Test
    public void emptyResultTest() {
        final List<ServerConfig> serverConfigs = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            serverConfigs.add(
                    new ServerConfig("test" + i, 3130 + i, "/path", ResponseFormat.XML, 0)
            );
        }
        final List<EmptyResultTestServer> servers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            servers.add(new EmptyResultTestServer(serverConfigs.get(i)));
        }
        final ActorSystem testSystem = ActorSystem.create("TestSystem");
        final ActorRef testActor = testSystem.actorOf(Props.create(MasterActor.class, new ActorConfig(testSystem), serverConfigs));
        final Timeout timeout = Timeout.create(Duration.ofDays(1));
        final Future<Object> future = Patterns.ask(testActor, new RequestMessage("test"), timeout);
        final String result;
        try {
            result = (String) Await.result(future, timeout.duration());
        } catch (final Exception e) {
            Assert.fail(e.getMessage());
            return;
        }
        final StringBuilder expected = new StringBuilder();
        for (int i = 1; i <= 3; i++) {
            expected.append("test").append(i).append(System.lineSeparator()).append(System.lineSeparator());
        }
        Assert.assertEquals(expected.toString(), result);
        for (EmptyResultTestServer server : servers) {
            server.stop();
        }
    }

    @Test
    public void fewResultsTest() {
        final List<ServerConfig> serverConfigs = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            serverConfigs.add(
                    new ServerConfig("test" + i, 3133 + i, "/path", ResponseFormat.XML, 0)
            );
        }
        final List<FewResultsTestServer> servers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            servers.add(new FewResultsTestServer(serverConfigs.get(i), i + 1));
        }
        final ActorSystem testSystem = ActorSystem.create("TestSystem");
        final ActorRef testActor = testSystem.actorOf(Props.create(MasterActor.class, new ActorConfig(testSystem), serverConfigs));
        final Timeout timeout = Timeout.create(Duration.ofDays(1));
        final Future<Object> future = Patterns.ask(testActor, new RequestMessage("test"), timeout);
        final String result;
        try {
            result = (String) Await.result(future, timeout.duration());
        } catch (final Exception e) {
            Assert.fail(e.getMessage());
            return;
        }
        final StringBuilder expected = new StringBuilder();
        for (int i = 1; i <= 3; i++) {
            expected.append("test").append(i).append(System.lineSeparator());
            for (int j = 1; j <= i; j++) {
                expected.append("    ").append("yandex").append(j).append(".ru").append(System.lineSeparator());
            }
            expected.append(System.lineSeparator());
        }
        Assert.assertEquals(expected.toString(), result);
        for (FewResultsTestServer server : servers) {
            server.stop();
        }
    }

    @Test
    public void testMasterActor() {
        final List<ServerConfig> serverConfigs = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            serverConfigs.add(
                    new ServerConfig("test" + i, 3136 + i, "/path", ResponseFormat.JSON, 0)
            );
        }
        final List<GoodTestServer> servers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            servers.add(new GoodTestServer(serverConfigs.get(i)));
        }
        final ActorSystem testSystem = ActorSystem.create("TestSystem");
        final ActorRef testActor = testSystem.actorOf(Props.create(MasterActor.class, new ActorConfig(testSystem), serverConfigs));
        final Timeout timeout = Timeout.create(Duration.ofDays(1));
        final Future<Object> future = Patterns.ask(testActor, new RequestMessage("test"), timeout);
        final String result;
        try {
            result = (String) Await.result(future, timeout.duration());
        } catch (final Exception e) {
            Assert.fail(e.getMessage());
            return;
        }
        final StringBuilder expected = new StringBuilder();
        for (int i = 1; i <= 3; i++) {
            expected.append("test").append(i).append(System.lineSeparator());
            expected.append("    ").append("yandex.ru").append(System.lineSeparator());
            expected.append("    ").append("google.com").append(System.lineSeparator());
            expected.append("    ").append("bing.com").append(System.lineSeparator());
            expected.append("    ").append("mail.ru").append(System.lineSeparator());
            expected.append("    ").append("yahoo.com").append(System.lineSeparator());
            expected.append(System.lineSeparator());
        }
        Assert.assertEquals(expected.toString(), result);
        for (GoodTestServer server : servers) {
            server.stop();
        }
    }

    @Test
    public void timeoutChildTest() {
        final List<ServerConfig> serverConfigs = new ArrayList<>();
        serverConfigs.add(new ServerConfig("test1", 3237, "/path", ResponseFormat.JSON, 60000));
        for (int i = 2; i <= 3; i++) {
            serverConfigs.add(
                    new ServerConfig("test" + i, 3236 + i, "/path", ResponseFormat.JSON, 0)
            );
        }
        final List<GoodTestServer> servers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            servers.add(new GoodTestServer(serverConfigs.get(i)));
        }
        final ActorSystem testSystem = ActorSystem.create("TestSystem");
        final ActorRef testActor = testSystem.actorOf(Props.create(MasterActor.class, new ActorConfig(testSystem), serverConfigs));
        final Timeout timeout = Timeout.create(Duration.ofDays(1));
        final Future<Object> future = Patterns.ask(testActor, new RequestMessage("test"), timeout);
        final String result;
        try {
            result = (String) Await.result(future, timeout.duration());
        } catch (final Exception e) {
            Assert.fail(e.getMessage());
            return;
        }
        final StringBuilder expected = new StringBuilder();
        for (int i = 2; i <= 3; i++) {
            expected.append("test").append(i).append(System.lineSeparator());
            expected.append("    ").append("yandex.ru").append(System.lineSeparator());
            expected.append("    ").append("google.com").append(System.lineSeparator());
            expected.append("    ").append("bing.com").append(System.lineSeparator());
            expected.append("    ").append("mail.ru").append(System.lineSeparator());
            expected.append("    ").append("yahoo.com").append(System.lineSeparator());
            expected.append(System.lineSeparator());
        }
        Assert.assertEquals(expected.toString(), result);
        for (GoodTestServer server : servers) {
            server.stop();
        }
    }
}
