package report;

import clock.SettableClock;
import database.Database;
import database.TypeOfTurnstileEvent;
import events.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import server.ServerConfig;
import server.ServerUtils;
import server.TestContext;
import server.TestServer;

import java.time.Instant;
import java.util.List;

public class ReportTests {
    private final SettableClock testClock = new SettableClock(Instant.now());
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

    private String getStatistics(final int membershipId) {
        return ServerUtils.readAsText("http://localhost:" + testReportsConfig.getPort() + "/" +
                "get_statistics" + "?" +
                "membership_id=" + membershipId);
    }

    @Test
    public void onlyMembershipTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage)),
                new TestContext("/get_statistics", new ReportStatistics(localStorage))));
        registerMembership(1, "John");
        final String result = getStatistics(1);
        Assertions.assertEquals(
                "Statistics for " + "John" + System.lineSeparator() +
                        "Visited " + 0 + " times," + " summary time: " + 0 + System.lineSeparator(),
                result);
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void getStatisticsForNotExistingMemberTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/get_statistics", new ReportStatistics(localStorage))));
        final String result = getStatistics(2);
        Assertions.assertEquals(
                "Can't get statistics: Membership id = " + 2 + " is not found" + System.lineSeparator(),
                result);
        testReportsServer.stop();
    }

    private void renewMembership(final int membershipId, final int eventId, final int validTillInSeconds) {
        ServerUtils.readAsText("http://localhost:" + testEventsConfig.getPort() + "/" +
                "renew_membership" + "?" +
                "password=" + testEventsConfig.getPassword() + "&" +
                "membership_id=" + membershipId + "&" +
                "event_id=" + eventId + "&" +
                "valid_till=" + validTillInSeconds);
    }

    private void newTurnstileEvent(final int membershipId, final int eventId, final TypeOfTurnstileEvent event) {
        ServerUtils.readAsText("http://localhost:" + testEventsConfig.getPort() + "/" +
                "new_turnstile_event" + "?" +
                "password=" + testEventsConfig.getPassword() + "&" +
                "membership_id=" + membershipId + "&" +
                "event_id=" + eventId + "&" +
                "event=" + event.toString().toLowerCase());
    }

    @Test
    public void getStatisticsForManyEventsTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/renew_membership", new RenewMembership(testEventsConfig, database)),
                new TestContext("/new_turnstile_event", new NewTurnstileEvent(testEventsConfig, testReportsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage)),
                new TestContext("/get_statistics", new ReportStatistics(localStorage)),
                new TestContext("/enter", new ReportEnter(testReportsConfig, localStorage)),
                new TestContext("/exit", new ReportExit(testReportsConfig, localStorage))));
        testClock.setNow(Instant.ofEpochSecond(1_000_000_000));
        registerMembership(2, "Tom");
        renewMembership(2, 1, 1_500_000_000);
        for (int i = 1; i <= 10; i++) {
            final TypeOfTurnstileEvent event = (i % 2 == 1) ? TypeOfTurnstileEvent.ENTER : TypeOfTurnstileEvent.EXIT;
            testClock.setNow(Instant.ofEpochSecond(1_000_000_000 + (i + 1) / 2 * 86_400 + (i + 1) % 2 * 100));
            newTurnstileEvent(2, i, event);
        }
        final String result = getStatistics(2);
        Assertions.assertEquals(
                "Statistics for " + "Tom" + System.lineSeparator() +
                        "On day " + 11575 + " member visited 1 times" + System.lineSeparator() +
                        "On day " + 11576 + " member visited 1 times" + System.lineSeparator() +
                        "On day " + 11577 + " member visited 1 times" + System.lineSeparator() +
                        "On day " + 11578 + " member visited 1 times" + System.lineSeparator() +
                        "On day " + 11579 + " member visited 1 times" + System.lineSeparator() +
                        "Visited " + 5 + " times, summary time: " + 500 + System.lineSeparator(),
                result);
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void progressTest() {
        ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/renew_membership", new RenewMembership(testEventsConfig, database)),
                new TestContext("/get_memberships", new GetMemberships(testEventsConfig, database)),
                new TestContext("/get_turnstile_events", new GetTurnstileEvents(testEventsConfig, database)),
                new TestContext("/new_turnstile_event", new NewTurnstileEvent(testEventsConfig, testReportsConfig, database))));
        TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage)),
                new TestContext("/get_statistics", new ReportStatistics(localStorage)),
                new TestContext("/enter", new ReportEnter(testReportsConfig, localStorage)),
                new TestContext("/exit", new ReportExit(testReportsConfig, localStorage))));
        testClock.setNow(Instant.ofEpochSecond(1_000_000_000));
        registerMembership(3, "John");
        renewMembership(3, 2, 1_200_000_000);
        testClock.setNow(Instant.ofEpochSecond(1_000_500_000));
        newTurnstileEvent(3, 1, TypeOfTurnstileEvent.ENTER);
        testClock.setNow(Instant.ofEpochSecond(1_000_500_100));
        newTurnstileEvent(3, 2, TypeOfTurnstileEvent.EXIT);

        testReportsServer.stop();

        localStorage = new ReportLocalStorage(testEventsConfig);
        testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage)),
                new TestContext("/get_statistics", new ReportStatistics(localStorage)),
                new TestContext("/enter", new ReportEnter(testReportsConfig, localStorage)),
                new TestContext("/exit", new ReportExit(testReportsConfig, localStorage))));

        testClock.setNow(Instant.ofEpochSecond(1_000_500_200));
        newTurnstileEvent(3, 3, TypeOfTurnstileEvent.ENTER);
        testClock.setNow(Instant.ofEpochSecond(1_000_500_400));
        newTurnstileEvent(3, 4, TypeOfTurnstileEvent.EXIT);

        final String result = getStatistics(3);
        Assertions.assertEquals(
                "Statistics for " + "John" + System.lineSeparator() +
                        "On day " + 11579 + " member visited 2 times" + System.lineSeparator() +
                        "Visited " + 2 + " times, summary time: " + 300 + System.lineSeparator(),
                result);

        testEventsServer.stop();
        testReportsServer.stop();
    }
}
