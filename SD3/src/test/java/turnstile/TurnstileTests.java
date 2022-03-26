package turnstile;

import clock.SettableClock;
import database.Database;
import events.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import report.AddMembership;
import report.ReportEnter;
import report.ReportExit;
import report.ReportLocalStorage;
import server.ServerConfig;
import server.ServerUtils;
import server.TestContext;
import server.TestServer;

import java.time.Instant;
import java.util.List;

public class TurnstileTests {
    private final SettableClock testClock = new SettableClock(Instant.now());
    private final ServerConfig testTurnstileConfig = new ServerConfig(33361, "password", testClock);
    private final ServerConfig testEventsConfig = new ServerConfig(33381, "123456", testClock);
    private final ServerConfig testReportsConfig = new ServerConfig(33391, "qwerty", testClock);

    private Database database;

    @Before
    public void beforeTests() {
        database = new Database("test", "--drop-old-tables");
    }

    private void registerMembership(final int membershipId, final String membershipName) {
        ServerUtils.readAsText("http://localhost:" + testEventsConfig.getPort() + "/" +
                "register_membership" + "?" +
                "password=" + testEventsConfig.getPassword() + "&" +
                "membership_id=" + membershipId + "&" +
                "membership_name=" + membershipName);
    }

    private void renewMembership(final int membershipId, final int eventId, final int validTillInSeconds) {
        ServerUtils.readAsText("http://localhost:" + testEventsConfig.getPort() + "/" +
                "renew_membership" + "?" +
                "password=" + testEventsConfig.getPassword() + "&" +
                "membership_id=" + membershipId + "&" +
                "event_id=" + eventId + "&" +
                "valid_till=" + validTillInSeconds);
    }

    private String enter(final int membershipId) {
        return ServerUtils.readAsText("http://localhost:" + testTurnstileConfig.getPort() + "/" +
                "enter" + "?" +
                "membership_id=" + membershipId);
    }

    @Test
    public void enterTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testTurnstileServer = new TestServer(testTurnstileConfig, List.of(
                new TestContext("/enter", new TurnstileEnter(testTurnstileConfig, testEventsConfig))));
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/renew_membership", new RenewMembership(testEventsConfig, database)),
                new TestContext("/new_turnstile_event", new NewTurnstileEvent(testEventsConfig, testReportsConfig, database)),
                new TestContext("/get_turnstile_events", new GetTurnstileEvents(testEventsConfig, database)),
                new TestContext("/get_membership_events", new GetMembershipEvents(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage)),
                new TestContext("/enter", new ReportEnter(testReportsConfig, localStorage))));
        testClock.setNow(Instant.ofEpochSecond(50));
        registerMembership(1, "test");
        renewMembership(1, 1, 100);
        final String result = enter(1);
        Assertions.assertEquals(
                "Membership: id = " + 1 + " passed" + System.lineSeparator(),
                result);
        testTurnstileServer.stop();
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void enterExpiredTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testTurnstileServer = new TestServer(testTurnstileConfig, List.of(
                new TestContext("/enter", new TurnstileEnter(testTurnstileConfig, testEventsConfig))));
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/renew_membership", new RenewMembership(testEventsConfig, database)),
                new TestContext("/new_turnstile_event", new NewTurnstileEvent(testEventsConfig, testReportsConfig, database)),
                new TestContext("/get_turnstile_events", new GetTurnstileEvents(testEventsConfig, database)),
                new TestContext("/get_membership_events", new GetMembershipEvents(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage)),
                new TestContext("/enter", new ReportEnter(testReportsConfig, localStorage))));
        testClock.setNow(Instant.ofEpochSecond(100));
        registerMembership(2, "Tom");
        renewMembership(2, 2, 200);
        testClock.setNow(Instant.ofEpochSecond(300));
        final String result = enter(2);
        Assertions.assertEquals(
                "Can't enter: The membership is expired" + System.lineSeparator(),
                result);
        testTurnstileServer.stop();
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void enterNotExistingMembershipTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testTurnstileServer = new TestServer(testTurnstileConfig, List.of(
                new TestContext("/enter", new TurnstileEnter(testTurnstileConfig, testEventsConfig))));
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/new_turnstile_event", new NewTurnstileEvent(testEventsConfig, testReportsConfig, database)),
                new TestContext("/get_turnstile_events", new GetTurnstileEvents(testEventsConfig, database)),
                new TestContext("/get_membership_events", new GetMembershipEvents(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage)),
                new TestContext("/enter", new ReportEnter(testReportsConfig, localStorage))));
        final String result = enter(2);
        Assertions.assertEquals(
                "Can't enter: Can't get membership events: Can't find membership: id = " + 2 + System.lineSeparator(),
                result);
        testTurnstileServer.stop();
        testEventsServer.stop();
        testReportsServer.stop();
    }

    private String exit(final int membershipId) {
        return ServerUtils.readAsText("http://localhost:" + testTurnstileConfig.getPort() + "/" +
                "exit" + "?" +
                "membership_id=" + membershipId);
    }

    @Test
    public void exitTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testTurnstileServer = new TestServer(testTurnstileConfig, List.of(
                new TestContext("/exit", new TurnstileExit(testEventsConfig))));
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/renew_membership", new RenewMembership(testEventsConfig, database)),
                new TestContext("/new_turnstile_event", new NewTurnstileEvent(testEventsConfig, testReportsConfig, database)),
                new TestContext("/get_turnstile_events", new GetTurnstileEvents(testEventsConfig, database)),
                new TestContext("/get_membership_events", new GetMembershipEvents(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage)),
                new TestContext("/exit", new ReportExit(testReportsConfig, localStorage))));
        testClock.setNow(Instant.ofEpochSecond(50));
        registerMembership(1, "test");
        renewMembership(1, 1, 100);
        final String result = exit(1);
        Assertions.assertEquals(
                "Membership: id = " + 1 + " exited" + System.lineSeparator(),
                result);
        testTurnstileServer.stop();
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void exitNotExistingMembershipTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testTurnstileServer = new TestServer(testTurnstileConfig, List.of(
                new TestContext("/exit", new TurnstileExit(testEventsConfig))));
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/new_turnstile_event", new NewTurnstileEvent(testEventsConfig, testReportsConfig, database)),
                new TestContext("/get_turnstile_events", new GetTurnstileEvents(testEventsConfig, database)),
                new TestContext("/get_membership_events", new GetMembershipEvents(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/exit", new ReportExit(testReportsConfig, localStorage))));
        final String result = exit(2);
        Assertions.assertEquals(
                "Can't exit: Can't find membership: id = " + 2 + System.lineSeparator(),
                result);
        testTurnstileServer.stop();
        testEventsServer.stop();
        testReportsServer.stop();
    }
}
