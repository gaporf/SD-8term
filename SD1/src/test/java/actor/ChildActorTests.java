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

public class ChildActorTests {
    @Test
    public void emptySearchResult() {
        final ServerConfig testConfig = new ServerConfig("test", 34342, "/path", ResponseFormat.JSON, 0);
        final EmptyResultTestServer emptyResultTestServer = new EmptyResultTestServer(testConfig);
        final ActorSystem testSystem = ActorSystem.create("TestSystem");
        final ActorRef testActor = testSystem.actorOf(Props.create(ChildActor.class, testConfig));
        final Timeout timeout = Timeout.create(Duration.ofSeconds(1));
        final Future<Object> future = Patterns.ask(testActor, new RequestMessage("test"), timeout);
        final String result;
        try {
            result = (String) Await.result(future, timeout.duration());
        } catch (final Exception e) {
            Assert.fail(e.getMessage());
            return;
        }
        Assert.assertEquals("test" + System.lineSeparator(), result);
        emptyResultTestServer.stop();
    }

    @Test
    public void fewSearchResults() {
        final ServerConfig testConfig = new ServerConfig("test", 34351, "/path", ResponseFormat.XML, 0);
        final StringBuilder expectedResults = new StringBuilder("test" + System.lineSeparator());
        final ActorSystem testSystem = ActorSystem.create("TestSystem");
        for (int i = 1; i <= 5; i++) {
            final FewResultsTestServer fewResultsTestServer = new FewResultsTestServer(testConfig, i);
            final ActorRef testActor = testSystem.actorOf(Props.create(ChildActor.class, testConfig));
            final Timeout timeout = Timeout.create(Duration.ofSeconds(1));
            final Future<Object> future = Patterns.ask(testActor, new RequestMessage("test"), timeout);
            final String result;
            try {
                result = (String) Await.result(future, timeout.duration());
            } catch (final Exception e) {
                Assert.fail(e.getMessage());
                return;
            }
            expectedResults.append("    ").append("yandex").append(i).append(".ru").append(System.lineSeparator());
            Assert.assertEquals(expectedResults.toString(), result);
            testSystem.stop(testActor);
            fewResultsTestServer.stop();
        }
    }

    @Test
    public void testChildActor() {
        final ServerConfig testConfig = new ServerConfig("test", 34361, "/path", ResponseFormat.JSON, 0);
        final GoodTestServer goodTestServer = new GoodTestServer(testConfig);
        final ActorSystem testSystem = ActorSystem.create("TestSystem");
        final ActorRef testActor = testSystem.actorOf(Props.create(ChildActor.class, testConfig));
        final Timeout timeout = Timeout.create(Duration.ofSeconds(1));
        final Future<Object> future = Patterns.ask(testActor, new RequestMessage("test"), timeout);
        final String result;
        try {
            result = (String) Await.result(future, timeout.duration());
        } catch (final Exception e) {
            Assert.fail(e.getMessage());
            return;
        }
        final String expectedResponse =
                "test" + System.lineSeparator() +
                        "    yandex.ru" + System.lineSeparator() +
                        "    google.com" + System.lineSeparator() +
                        "    bing.com" + System.lineSeparator() +
                        "    mail.ru" + System.lineSeparator() +
                        "    yahoo.com" + System.lineSeparator();
        Assert.assertEquals(expectedResponse, result);
        goodTestServer.stop();
    }

    @Test
    public void incorrectConfigTest1() {
        final ServerConfig rightConfig = new ServerConfig("right", 3338, "/right", ResponseFormat.JSON, 0);
        final SearchServer searchServer = new SearchServer(rightConfig);
        final ServerConfig wrongConfig = new ServerConfig("wrong", 3338, "/wrong", ResponseFormat.JSON, 0);
        final ActorSystem testSystem = ActorSystem.create("TestSystem");
        final ActorRef testActor = testSystem.actorOf(Props.create(ChildActor.class, wrongConfig));
        final Timeout timeout = Timeout.create(Duration.ofSeconds(1));
        final Future<Object> future = Patterns.ask(testActor, new RequestMessage("test"), timeout);
        final String result;
        try {
            result = (String) Await.result(future, timeout.duration());
        } catch (final Exception e) {
            Assert.fail(e.getMessage());
            return;
        }
        Assert.assertEquals("", result);
        searchServer.stop();
    }

    @Test
    public void incorrectConfigTest2() {
        final ServerConfig rightConfig = new ServerConfig("right", 3339, "/right", ResponseFormat.JSON, 0);
        final SearchServer searchServer = new SearchServer(rightConfig);
        final ServerConfig wrongConfig = new ServerConfig("wrong", 3339, "/right", ResponseFormat.XML, 0);
        final ActorSystem testSystem = ActorSystem.create("TestSystem");
        final ActorRef testActor = testSystem.actorOf(Props.create(ChildActor.class, wrongConfig));
        final Timeout timeout = Timeout.create(Duration.ofSeconds(1));
        final Future<Object> future = Patterns.ask(testActor, new RequestMessage("test"), timeout);
        final String result;
        try {
            result = (String) Await.result(future, timeout.duration());
        } catch (final Exception e) {
            Assert.fail(e.getMessage());
            return;
        }
        Assert.assertEquals("", result);
        searchServer.stop();
    }

    @Test
    public void incorrectConfigTest3() {
        final ServerConfig rightConfig = new ServerConfig("right", 3337, "/right", ResponseFormat.XML, 0);
        final SearchServer searchServer = new SearchServer(rightConfig);
        final ServerConfig wrongConfig = new ServerConfig("wrong", 3337, "/right", ResponseFormat.JSON, 0);
        final ActorSystem testSystem = ActorSystem.create("TestSystem");
        final ActorRef testActor = testSystem.actorOf(Props.create(ChildActor.class, wrongConfig));
        final Timeout timeout = Timeout.create(Duration.ofSeconds(1));
        final Future<Object> future = Patterns.ask(testActor, new RequestMessage("test"), timeout);
        final String result;
        try {
            result = (String) Await.result(future, timeout.duration());
        } catch (final Exception e) {
            Assert.fail(e.getMessage());
            return;
        }
        Assert.assertEquals("", result);
        searchServer.stop();
    }
}
