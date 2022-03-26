package manager;

import clock.SettableClock;
import database.Database;
import events.GetMembershipEvents;
import events.RegisterMembership;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import report.AddMembership;
import report.ReportLocalStorage;
import server.ServerConfig;
import server.ServerUtils;
import server.TestContext;
import server.TestServer;

import java.time.Instant;
import java.util.List;

public class ManagerTests {
    private final SettableClock testClock = new SettableClock(Instant.now());
    private final ServerConfig testManagerConfig = new ServerConfig(33371, "password", testClock);
    private final ServerConfig testEventsConfig = new ServerConfig(33381, "123456", testClock);
    private final ServerConfig testReportsConfig = new ServerConfig(33391, "qwerty", testClock);

    private Database database;

    @Before
    public void beforeTests() {
        database = new Database("test", "--drop-old-tables");
    }

    private String giveMembership(final int membershipId, final String membershipName) {
        return ServerUtils.readAsText("http://localhost:" + testManagerConfig.getPort() + "/" +
                "give_membership" + "?" +
                "membership_id=" + membershipId + "&" +
                "membership_name=" + membershipName);
    }

    @Test
    public void giveMembershipTest() {
        final TestServer testManagerServer = new TestServer(testManagerConfig, List.of(
                new TestContext("/give_membership", new GiveMembership(testEventsConfig))));
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, new ReportLocalStorage()))));
        final String result = giveMembership(1, "test");
        Assertions.assertEquals("Membership is given: id = " + 1 + System.lineSeparator(), result);
        testManagerServer.stop();
        testEventsServer.stop();
        testReportsServer.stop();
    }

    private String membershipInfo(final int membershipId) {
        return ServerUtils.readAsText("http://localhost:" + testManagerConfig.getPort() + "/" +
                "membership_info" + "?" +
                "membership_id=" + membershipId);
    }

    @Test
    public void giveAndGetMembershipInfoTest() {
        final TestServer testManagerServer = new TestServer(testManagerConfig, List.of(
                new TestContext("/give_membership", new GiveMembership(testEventsConfig)),
                new TestContext("/membership_info", new MembershipInfo(testEventsConfig))));
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/get_membership_events", new GetMembershipEvents(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, new ReportLocalStorage()))));
        testClock.setNow(Instant.ofEpochSecond(678));
        giveMembership(1, "test");
        final String result = membershipInfo(1);
        Assertions.assertEquals(
                "Info for membership id = " + 1 + System.lineSeparator() +
                        "Created at " + 678 + System.lineSeparator() +
                        "The membership is not activated" + System.lineSeparator(),
                result);
        testManagerServer.stop();
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void giveManyMembershipsTest() {
        final TestServer testManagerServer = new TestServer(testManagerConfig, List.of(
                new TestContext("/give_membership", new GiveMembership(testEventsConfig)),
                new TestContext("/membership_info", new MembershipInfo(testEventsConfig))));
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/get_membership_events", new GetMembershipEvents(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, new ReportLocalStorage()))));
        for (int i = 1; i <= 20; i++) {
            testClock.setNow(Instant.ofEpochSecond(i));
            giveMembership(i, Integer.toString(i));
            final String result = membershipInfo(i);
            Assertions.assertEquals(
                    "Info for membership id = " + i + System.lineSeparator() +
                            "Created at " + i + System.lineSeparator() +
                            "The membership is not activated" + System.lineSeparator(),
                    result);
        }
        testManagerServer.stop();
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void giveMembershipsWithSameIdsTest() {
        final TestServer testManagerServer = new TestServer(testManagerConfig, List.of(
                new TestContext("/give_membership", new GiveMembership(testEventsConfig))));
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, new ReportLocalStorage()))));
        giveMembership(1, "test");
        final String result = giveMembership(1, "test2");
        Assertions.assertEquals(
                "Can't give membership: Can't add membership: Membership with id = " + 1 + " is already added" + System.lineSeparator(),
                result);
        testManagerServer.stop();
        testEventsServer.stop();
        testReportsServer.stop();
    }

    private String renewMembership(final int membershipId, final int eventId, final int validTillInSeconds) {
        return ServerUtils.readAsText("http://localhost:" + testEventsConfig.getPort() + "/" +
                "renew_membership" + "?" +
                "password=" + testEventsConfig.getPassword() + "&" +
                "membership_id=" + membershipId + "&" +
                "event_id=" + eventId + "&" +
                "valid_till=" + validTillInSeconds);
    }

    @Test
    public void renewMembershipTest() {
        final TestServer testManagerServer = new TestServer(testManagerConfig, List.of(
                new TestContext("/give_membership", new GiveMembership(testEventsConfig)),
                new TestContext("/renew_membership", new RenewMembership(testEventsConfig))));
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/renew_membership", new events.RenewMembership(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, new ReportLocalStorage()))));
        giveMembership(1, "test");
        final String result = renewMembership(1, 1, 100);
        Assertions.assertEquals(
                "MembershipEvent for membership id = " + 1 +
                        " and event id = " + 1 + " is added" + System.lineSeparator(),
                result);
        testManagerServer.stop();
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void renewAndGetMembershipTest() {
        final TestServer testManagerServer = new TestServer(testManagerConfig, List.of(
                new TestContext("/give_membership", new GiveMembership(testEventsConfig)),
                new TestContext("/renew_membership", new RenewMembership(testEventsConfig)),
                new TestContext("/membership_info", new MembershipInfo(testEventsConfig))));
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/renew_membership", new events.RenewMembership(testEventsConfig, database)),
                new TestContext("/get_membership_events", new GetMembershipEvents(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, new ReportLocalStorage()))));
        testClock.setNow(Instant.ofEpochSecond(100));
        giveMembership(1, "test");
        testClock.setNow(Instant.ofEpochSecond(200));
        renewMembership(1, 1, 300);
        final String result = membershipInfo(1);
        Assertions.assertEquals(
                "Info for membership id = " + 1 + System.lineSeparator() +
                        "Created at " + 100 + System.lineSeparator() +
                        "valid till " + 300 + System.lineSeparator(),
                result);
        testManagerServer.stop();
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void renewSeveralTimesTest() {
        final TestServer testManagerServer = new TestServer(testManagerConfig, List.of(
                new TestContext("/give_membership", new GiveMembership(testEventsConfig)),
                new TestContext("/renew_membership", new RenewMembership(testEventsConfig)),
                new TestContext("/membership_info", new MembershipInfo(testEventsConfig))));
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/renew_membership", new events.RenewMembership(testEventsConfig, database)),
                new TestContext("/get_membership_events", new GetMembershipEvents(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, new ReportLocalStorage()))));
        testClock.setNow(Instant.ofEpochSecond(1000));
        giveMembership(1, "test");
        for (int i = 1; i <= 15; i++) {
            testClock.setNow(Instant.ofEpochSecond(1000 + 10 * i));
            renewMembership(1, i, 1000 * i);
        }
        final String result = membershipInfo(1);
        Assertions.assertEquals(
                "Info for membership id = " + 1 + System.lineSeparator() +
                        "Created at " + 1000 + System.lineSeparator() +
                        "valid till " + 15000 + System.lineSeparator(),
                result);
        testManagerServer.stop();
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void renewWithSameEventIdsTest() {
        final TestServer testManagerServer = new TestServer(testManagerConfig, List.of(
                new TestContext("/give_membership", new GiveMembership(testEventsConfig)),
                new TestContext("/renew_membership", new RenewMembership(testEventsConfig))));
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/renew_membership", new events.RenewMembership(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, new ReportLocalStorage()))));
        giveMembership(1, "test");
        renewMembership(1, 1, 1);
        final String result = renewMembership(1, 1, 1);
        Assertions.assertEquals(
                "Can't renew membership: Membership event id = " + 1 +
                        " for membership id = " + 1 + " is already added" + System.lineSeparator(),
                result);
        testManagerServer.stop();
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void renewSeveralMembershipsTest() {
        final TestServer testManagerServer = new TestServer(testManagerConfig, List.of(
                new TestContext("/give_membership", new GiveMembership(testEventsConfig)),
                new TestContext("/renew_membership", new RenewMembership(testEventsConfig)),
                new TestContext("/membership_info", new MembershipInfo(testEventsConfig))));
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/renew_membership", new events.RenewMembership(testEventsConfig, database)),
                new TestContext("/get_membership_events", new GetMembershipEvents(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, new ReportLocalStorage()))));
        for (int i = 1; i < 10; i++) {
            testClock.setNow(Instant.ofEpochSecond(100 * i));
            giveMembership(i, Integer.toString(i));
            for (int j = 1; j <= 10 - i; j++) {
                testClock.setNow(Instant.ofEpochSecond(200 * i + 50L * j));
                renewMembership(i, j, 1000 * j);
            }
            final String result = membershipInfo(i);
            Assertions.assertEquals(
                    "Info for membership id = " + i + System.lineSeparator() +
                            "Created at " + 100 * i + System.lineSeparator() +
                            "valid till " + 1000 * (10 - i) + System.lineSeparator(),
                    result);
        }
        testManagerServer.stop();
        testEventsServer.stop();
        testReportsServer.stop();
    }
}
