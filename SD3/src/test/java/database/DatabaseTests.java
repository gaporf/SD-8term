package database;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.List;

public class DatabaseTests {
    private Database database;

    @Before
    public void beforeTests() {
        database = new Database("test", "--drop-old-tables");
    }

    @Test
    public void addMembership() {
        database.addMembership(new Membership(1, "test", 1));
    }

    @Test
    public void addAndCheckMembership() {
        database.addMembership(new Membership(1, "test", 1));
        final Membership membership = database.getMembership(1);
        Assertions.assertEquals(membership.getId(), 1);
        Assertions.assertEquals(membership.getName(), "test");
        Assertions.assertEquals(membership.getAddedTimeInSeconds(), 1);
    }

    @Test
    public void checkNotExistingMembership() {
        Assertions.assertThrows(DatabaseException.class, () -> database.getMembership(1));
    }

    @Test
    public void getMembershipsZero() {
        Assertions.assertEquals(List.of(), database.getMemberships());
    }

    @Test
    public void getMembershipsOne() {
        final List<Membership> memberships = List.of(new Membership(1, "test", 1));
        database.addMembership(memberships.get(0));
        Assertions.assertEquals(memberships, database.getMemberships());
    }

    @Test
    public void getMembershipsMany() {
        final List<Membership> memberships = List.of(
                new Membership(1, "first", 1),
                new Membership(2, "second", 2),
                new Membership(3, "third", 3),
                new Membership(4, "fourth", 4));
        for (final Membership membership : memberships) {
            database.addMembership(membership);
        }
        Assertions.assertEquals(memberships, database.getMemberships());
    }

    @Test
    public void addSameMemberships() {
        database.addMembership(new Membership(1, "test", 1));
        Assertions.assertThrows(DatabaseException.class, () -> database.addMembership(new Membership(1, "test", 1)));
    }

    @Test
    public void getMembershipsOutOfOrder() {
        final List<Membership> memberships = List.of(
                new Membership(1, "first", 1),
                new Membership(2, "second", 2),
                new Membership(3, "third", 3));
        database.addMembership(memberships.get(0));
        database.addMembership(memberships.get(2));
        database.addMembership(memberships.get(1));
        Assertions.assertEquals(memberships, database.getMemberships());
    }

    @Test
    public void addMembershipEvent() {
        database.addMembership(new Membership(1, "test", 1));
        database.addMembershipEvent(new MembershipEvent(1, 1, 1, 1));
    }

    @Test
    public void getMembershipEvent() {
        database.addMembership(new Membership(1, "test", 1));
        final MembershipEvent membershipEvent = new MembershipEvent(1, 1, 1, 1);
        database.addMembershipEvent(membershipEvent);
        Assertions.assertEquals(database.getMembershipsEvents(1), List.of(membershipEvent));
    }

    @Test
    public void getMembershipEventsMany() {
        database.addMembership(new Membership(1, "test", 1));
        final List<MembershipEvent> membershipEvents = List.of(
                new MembershipEvent(1, 1, 1, 1),
                new MembershipEvent(2, 1, 1, 1),
                new MembershipEvent(3, 1, 1, 1));
        for (final MembershipEvent membershipEvent : membershipEvents) {
            database.addMembershipEvent(membershipEvent);
        }
        Assertions.assertEquals(database.getMembershipsEvents(1), membershipEvents);
    }

    @Test
    public void getMembershipEventsIfNoMembership() {
        Assertions.assertThrows(DatabaseException.class, () -> database.getMembershipsEvents(1));
    }

    @Test
    public void addMembershipEventsIfNoMembership() {
        Assertions.assertThrows(DatabaseException.class, () -> database.addMembershipEvent(new MembershipEvent(1, 1, 1, 1)));
    }

    @Test
    public void addSameMembershipEvents() {
        database.addMembership(new Membership(1, "test", 1));
        database.addMembershipEvent(new MembershipEvent(1, 1, 1, 1));
        Assertions.assertThrows(DatabaseException.class, () -> database.addMembershipEvent(new MembershipEvent(1, 1, 1, 1)));
    }

    @Test
    public void addMembershipEventsForDifferentMemberships() {
        final Membership first = new Membership(1, "first", 1);
        final Membership second = new Membership(2, "second", 2);
        database.addMembership(first);
        database.addMembership(second);
        final List<MembershipEvent> firstEvents = List.of(
                new MembershipEvent(1, 1, 1, 1),
                new MembershipEvent(2, 1, 1, 1),
                new MembershipEvent(3, 1, 1, 1));
        for (final MembershipEvent membershipEvent : firstEvents) {
            database.addMembershipEvent(membershipEvent);
        }
        final List<MembershipEvent> secondEvents = List.of(
                new MembershipEvent(1, 2, 2, 2),
                new MembershipEvent(2, 2, 2, 2));
        for (final MembershipEvent membershipEvent : secondEvents) {
            database.addMembershipEvent(membershipEvent);
        }
        Assertions.assertEquals(database.getMembershipsEvents(1), firstEvents);
        Assertions.assertEquals(database.getMembershipsEvents(2), secondEvents);
    }

    @Test
    public void addTurnstileEvent() {
        database.addMembership(new Membership(1, "test", 1));
        database.addTurnstileEvent(new TurnstileEvent(1, 1, TypeOfTurnstileEvent.ENTER, 1));
    }

    @Test
    public void getTurnstileEventsOne() {
        database.addMembership(new Membership(1, "test", 1));
        final TurnstileEvent turnstileEvent = new TurnstileEvent(1, 1, TypeOfTurnstileEvent.EXIT, 1);
        database.addTurnstileEvent(turnstileEvent);
        Assertions.assertEquals(List.of(turnstileEvent), database.getTurnstileEvents(1));
    }

    @Test
    public void getTurnstileEventsMany() {
        database.addMembership(new Membership(1, "test", 1));
        final List<TurnstileEvent> turnstileEvents = List.of(
                new TurnstileEvent(1, 1, TypeOfTurnstileEvent.ENTER, 1),
                new TurnstileEvent(2, 1, TypeOfTurnstileEvent.EXIT, 10));
        for (final TurnstileEvent turnstileEvent : turnstileEvents) {
            database.addTurnstileEvent(turnstileEvent);
        }
        Assertions.assertEquals(turnstileEvents, database.getTurnstileEvents(1));
    }

    @Test
    public void getTurnstileEventsIfNoMembership() {
        Assertions.assertThrows(DatabaseException.class, () -> database.getTurnstileEvents(1));
    }

    @Test
    public void addTurnstileEventsIfNoMembership() {
        Assertions.assertThrows(DatabaseException.class, () -> database.addTurnstileEvent(new TurnstileEvent(1, 1, TypeOfTurnstileEvent.ENTER, 1)));
    }

    @Test
    public void addSameTurnstileEvents() {
        database.addMembership(new Membership(1, "test", 1));
        database.addTurnstileEvent(new TurnstileEvent(1, 1, TypeOfTurnstileEvent.ENTER, 1));
        Assertions.assertThrows(DatabaseException.class, () -> database.addTurnstileEvent(new TurnstileEvent(1, 1, TypeOfTurnstileEvent.ENTER, 1)));
    }

    @Test
    public void addTurnstileEventsForDifferentMemberships() {
        final Membership first = new Membership(1, "first", 1);
        final Membership second = new Membership(2, "second", 2);
        database.addMembership(first);
        database.addMembership(second);
        final List<TurnstileEvent> firstEvents = List.of(
                new TurnstileEvent(1, 1, TypeOfTurnstileEvent.ENTER, 1),
                new TurnstileEvent(2, 1, TypeOfTurnstileEvent.EXIT, 10),
                new TurnstileEvent(3, 1, TypeOfTurnstileEvent.ENTER, 100));
        for (final TurnstileEvent turnstileEvent : firstEvents) {
            database.addTurnstileEvent(turnstileEvent);
        }
        final List<TurnstileEvent> secondEvents = List.of(
                new TurnstileEvent(1, 2, TypeOfTurnstileEvent.ENTER, 2),
                new TurnstileEvent(2, 2, TypeOfTurnstileEvent.ENTER, 200));
        for (final TurnstileEvent turnstileEvent : secondEvents) {
            database.addTurnstileEvent(turnstileEvent);
        }
        Assertions.assertEquals(database.getTurnstileEvents(1), firstEvents);
        Assertions.assertEquals(database.getTurnstileEvents(2), secondEvents);
    }
}
