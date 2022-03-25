package events;

import clock.SettableClock;
import database.Database;
import database.TypeOfTurnstileEvent;
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

public class EventsServerTests {
    private final SettableClock testClock = new SettableClock(Instant.now());
    private final ServerConfig testEventsConfig = new ServerConfig(33381, "123456", testClock);
    private final ServerConfig testReportsConfig = new ServerConfig(33391, "qwerty", testClock);

    private Database database;

    @Before
    public void beforeTests() {
        database = new Database("test", "--drop-old-tables");
    }

    private String getMemberships() {
        return ServerUtils.readAsText("http://localhost:" + testEventsConfig.getPort() + "/" +
                "get_memberships" + "?" +
                "password=" + testEventsConfig.getPassword());
    }

    @Test
    public void getEmptyMembershipsTest() {
        final TestServer testServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/get_memberships", new GetMemberships(testEventsConfig, database))));
        final String result = getMemberships();
        Assertions.assertEquals("Info for memberships" + System.lineSeparator(), result);
        testServer.stop();
    }

    private String registerMembership(final int membershipId, final String membershipName) {
        return ServerUtils.readAsText("http://localhost:" + testEventsConfig.getPort() + "/" +
                "register_membership" + "?" +
                "password=" + testEventsConfig.getPassword() + "&" +
                "membership_id=" + membershipId + "&" +
                "membership_name=" + membershipName);
    }

    @Test
    public void registerMembershipTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage))));
        final String result = registerMembership(1, "test");
        Assertions.assertEquals("Membership: id = " + 1 + " is added" + System.lineSeparator(), result);
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void registerAndGetMembershipTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/get_memberships", new GetMemberships(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage))));
        testClock.setNow(Instant.ofEpochSecond(1000));
        registerMembership(1, "test");
        final String result = getMemberships();
        Assertions.assertEquals("Info for memberships" + System.lineSeparator() +
                "Membership: id = " + 1 + ", name = " + "test" + ", created at " + 1000 + System.lineSeparator(), result);
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void getManyMembershipsTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/get_memberships", new GetMemberships(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage))));
        final StringBuilder expectedBuilder = new StringBuilder("Info for memberships" + System.lineSeparator());
        for (int i = 1; i <= 10; i++) {
            testClock.setNow(Instant.ofEpochSecond(i));
            registerMembership(i, Integer.toString(i));
            expectedBuilder.append("Membership: id = ").append(i).append(",")
                    .append(" name = ").append(i).append(",")
                    .append(" created at ").append(i).append(System.lineSeparator());
        }
        final String result = getMemberships();
        Assertions.assertEquals(expectedBuilder.toString(), result);
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void addSameMembershipTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage))));
        registerMembership(1, "test");
        final String result = registerMembership(1, "test2");
        Assertions.assertEquals(
                "Can't add membership: Membership with id = 1 is already added" + System.lineSeparator()
                , result);
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
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/renew_membership", new RenewMembership(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage))));
        registerMembership(1, "test");
        final String result = renewMembership(1, 1, 100);
        Assertions.assertEquals(
                "MembershipEvent for membership id = " + 1 + " and event id = " + 1 + " is added" + System.lineSeparator(),
                result);
        testEventsServer.stop();
        testReportsServer.stop();
    }

    private String getMembershipEvents(final int membershipId) {
        return ServerUtils.readAsText("http://localhost:" + testEventsConfig.getPort() + "/" +
                "get_membership_events" + "?" +
                "password=" + testEventsConfig.getPassword() + "&" +
                "membership_id=" + membershipId);
    }

    @Test
    public void getNoMembershipEventsTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/get_membership_events", new GetMembershipEvents(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage))));
        testClock.setNow(Instant.ofEpochSecond(1234));
        registerMembership(1, "test");
        final String result = getMembershipEvents(1);
        Assertions.assertEquals(
                "Info for membership id = " + 1 + System.lineSeparator() +
                        "Created at " + 1234 + System.lineSeparator(), result);
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void renewAndGetMembershipEventTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/renew_membership", new RenewMembership(testEventsConfig, database)),
                new TestContext("/get_membership_events", new GetMembershipEvents(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage))));
        testClock.setNow(Instant.ofEpochSecond(100));
        registerMembership(1, "test");
        testClock.setNow(Instant.ofEpochSecond(200));
        renewMembership(1, 1, 1000);
        final String result = getMembershipEvents(1);
        Assertions.assertEquals(
                "Info for membership id = " + 1 + System.lineSeparator() +
                        "Created at " + 100 + System.lineSeparator() +
                        1 + ")" + " time: " + 200 + ", valid till " + 1000 + System.lineSeparator(),
                result);
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void renewAndGetMembershipEventsTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/renew_membership", new RenewMembership(testEventsConfig, database)),
                new TestContext("/get_membership_events", new GetMembershipEvents(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage))));
        testClock.setNow(Instant.ofEpochSecond(150));
        registerMembership(1, "test");
        final StringBuilder expectedBuilder = new StringBuilder(
                "Info for membership id = " + 1 + System.lineSeparator() +
                        "Created at " + 150 + System.lineSeparator());
        for (int i = 1; i <= 10; i++) {
            testClock.setNow(Instant.ofEpochSecond(200 * i));
            renewMembership(1, i, 300 * i);
            expectedBuilder.append(i).append(")")
                    .append(" time: ").append(200 * i)
                    .append(", valid till ").append(300 * i).append(System.lineSeparator());
        }
        final String result = getMembershipEvents(1);
        Assertions.assertEquals(expectedBuilder.toString(), result);
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void renewMembershipsAndGetMembershipEventsTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/renew_membership", new RenewMembership(testEventsConfig, database)),
                new TestContext("/get_membership_events", new GetMembershipEvents(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage))));
        for (int i = 1; i <= 10; i++) {
            testClock.setNow(Instant.ofEpochSecond(2000 * i));
            registerMembership(i, Integer.toString(i));
            final StringBuilder eventBuilder = new StringBuilder(
                    "Info for membership id = " + i + System.lineSeparator() +
                            "Created at " + 2000 * i + System.lineSeparator());
            for (int j = 1; j <= 10 - i; j++) {
                testClock.setNow(Instant.ofEpochSecond(2000 * i + 100L * j));
                renewMembership(i, j, 2000 * i + 100 * j + 50);
                eventBuilder.append(j).append(")")
                        .append(" time: ").append(2000 * i + 100L * j)
                        .append(", valid till ").append(2000 * i + 100 * j + 50).append(System.lineSeparator());
            }
            final String result = getMembershipEvents(i);
            Assertions.assertEquals(eventBuilder.toString(), result);
        }
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void renewNotExistingMembershipTest() {
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/renew_membership", new RenewMembership(testEventsConfig, database))));
        final String result = renewMembership(1, 1, 1);
        Assertions.assertEquals(
                "Can't renew membership: Can't find membership: id = " + 1 + System.lineSeparator(),
                result);
        testEventsServer.stop();
    }

    @Test
    public void renewWithSameIdsTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/renew_membership", new RenewMembership(testEventsConfig, database)),
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage))));
        registerMembership(1, "test");
        renewMembership(1, 1, 1);
        final String result = renewMembership(1, 1, 5);
        Assertions.assertEquals(
                "Can't renew membership: Membership event id = " + 1 +
                        " for membership id = " + 1 + " is already added" + System.lineSeparator(),
                result);
        testEventsServer.stop();
        testReportsServer.stop();
    }

    private String newTurnstileEvent(final int membershipId, final int eventId, final TypeOfTurnstileEvent event) {
        return ServerUtils.readAsText("http://localhost:" + testEventsConfig.getPort() + "/" +
                "new_turnstile_event" + "?" +
                "password=" + testEventsConfig.getPassword() + "&" +
                "membership_id=" + membershipId + "&" +
                "event_id=" + eventId + "&" +
                "event=" + event.toString().toLowerCase());
    }

    @Test
    public void newTurnstileEventTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/new_turnstile_event", new NewTurnstileEvent(testEventsConfig, testReportsConfig, database)),
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage)),
                new TestContext("/enter", new ReportEnter(testReportsConfig, localStorage))));
        registerMembership(1, "test");
        final String result = newTurnstileEvent(1, 1, TypeOfTurnstileEvent.ENTER);
        Assertions.assertEquals(
                "Turnstile event: id = " + 1 + " for membership: id = " + 1 + " is added" + System.lineSeparator(),
                result);
        testEventsServer.stop();
        testReportsServer.stop();
    }

    private String getTurnstileEvents(final int membershipId) {
        return ServerUtils.readAsText("http://localhost:" + testEventsConfig.getPort() + "/" +
                "get_turnstile_events" + "?" +
                "password=" + testEventsConfig.getPassword() + "&" +
                "membership_id=" + membershipId);
    }

    @Test
    public void getNoTurnstileEventsTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/get_turnstile_events", new GetTurnstileEvents(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage)),
                new TestContext("/enter", new ReportEnter(testReportsConfig, localStorage))));
        testClock.setNow(Instant.ofEpochSecond(22));
        registerMembership(1, "test");
        final String result = getTurnstileEvents(1);
        Assertions.assertEquals(
                "Info for membership id = " + 1 + System.lineSeparator() +
                        "Created at " + 22 + System.lineSeparator(),
                result);
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void newAndGetTurnstileEventTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/new_turnstile_event", new NewTurnstileEvent(testEventsConfig, testReportsConfig, database)),
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/get_turnstile_events", new GetTurnstileEvents(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage)),
                new TestContext("/enter", new ReportEnter(testReportsConfig, localStorage))));
        testClock.setNow(Instant.ofEpochSecond(65));
        registerMembership(1, "test");
        testClock.setNow(Instant.ofEpochSecond(125));
        newTurnstileEvent(1, 1, TypeOfTurnstileEvent.ENTER);
        final String result = getTurnstileEvents(1);
        Assertions.assertEquals(
                "Info for membership id = " + 1 + System.lineSeparator() +
                        "Created at " + 65 + System.lineSeparator() +
                        1 + ")" + " time: " + 125 + ", event " + "ENTER" + System.lineSeparator(),
                result);
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void newAndGetMembershipEventsTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/new_turnstile_event", new NewTurnstileEvent(testEventsConfig, testReportsConfig, database)),
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/get_turnstile_events", new GetTurnstileEvents(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage)),
                new TestContext("/enter", new ReportEnter(testReportsConfig, localStorage)),
                new TestContext("/exit", new ReportExit(testReportsConfig, localStorage))));
        testClock.setNow(Instant.ofEpochSecond(250));
        registerMembership(1, "test");
        final StringBuilder expectedBuilder = new StringBuilder(
                "Info for membership id = " + 1 + System.lineSeparator() +
                        "Created at " + 250 + System.lineSeparator());
        for (int i = 1; i <= 10; i++) {
            testClock.setNow(Instant.ofEpochSecond(400 * i));
            final TypeOfTurnstileEvent event = i % 2 == 1 ? TypeOfTurnstileEvent.ENTER : TypeOfTurnstileEvent.EXIT;
            newTurnstileEvent(1, i, event);
            expectedBuilder.append(i).append(")")
                    .append(" time: ").append(400 * i)
                    .append(", event ").append(event).append(System.lineSeparator());
        }
        final String result = getTurnstileEvents(1);
        Assertions.assertEquals(expectedBuilder.toString(), result);
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void membershipsAndGetTurnstileEventsTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/new_turnstile_event", new NewTurnstileEvent(testEventsConfig, testReportsConfig, database)),
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database)),
                new TestContext("/get_turnstile_events", new GetTurnstileEvents(testEventsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/add_membership", new AddMembership(testReportsConfig, localStorage)),
                new TestContext("/enter", new ReportEnter(testReportsConfig, localStorage)),
                new TestContext("/exit", new ReportExit(testReportsConfig, localStorage))));
        for (int i = 1; i <= 10; i++) {
            testClock.setNow(Instant.ofEpochSecond(2000 * i));
            registerMembership(i, Integer.toString(i));
            final StringBuilder eventBuilder = new StringBuilder(
                    "Info for membership id = " + i + System.lineSeparator() +
                            "Created at " + 2000 * i + System.lineSeparator());
            for (int j = 1; j <= 10 - i; j++) {
                testClock.setNow(Instant.ofEpochSecond(2000 * i + 100L * j));
                final TypeOfTurnstileEvent event = i % 2 == 1 ? TypeOfTurnstileEvent.ENTER : TypeOfTurnstileEvent.EXIT;
                newTurnstileEvent(i, j, event);
                eventBuilder.append(j).append(")")
                        .append(" time: ").append(2000 * i + 100L * j)
                        .append(", event ").append(event).append(System.lineSeparator());
            }
            final String result = getTurnstileEvents(i);
            Assertions.assertEquals(eventBuilder.toString(), result);
        }
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void newTurnstileEventNotExistingMembershipTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/new_turnstile_event", new NewTurnstileEvent(testEventsConfig, testReportsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/enter", new ReportEnter(testReportsConfig, localStorage))));
        final String result = newTurnstileEvent(1, 1, TypeOfTurnstileEvent.ENTER);
        Assertions.assertEquals(
                "Can't add turnstile event: Can't find membership: id = " + 1 + System.lineSeparator(),
                result);
        testEventsServer.stop();
        testReportsServer.stop();
    }

    @Test
    public void newTurnstileEventWithSameIdsTest() {
        final ReportLocalStorage localStorage = new ReportLocalStorage();
        final TestServer testEventsServer = new TestServer(testEventsConfig, List.of(
                new TestContext("/new_turnstile_event", new NewTurnstileEvent(testEventsConfig, testReportsConfig, database)),
                new TestContext("/register_membership", new RegisterMembership(testEventsConfig, testReportsConfig, database))));
        final TestServer testReportsServer = new TestServer(testReportsConfig, List.of(
                new TestContext("/enter", new ReportEnter(testReportsConfig, localStorage))));
        registerMembership(1, "test");
        newTurnstileEvent(1, 1, TypeOfTurnstileEvent.ENTER);
        final String result = newTurnstileEvent(1, 1, TypeOfTurnstileEvent.ENTER);
        Assertions.assertEquals(
                "Can't add turnstile event: Turnstile event id = " + 1 +
                        " for membership id = " + 1 + " is already added" + System.lineSeparator(),
                result);
        testEventsServer.stop();
        testReportsServer.stop();
    }
}
