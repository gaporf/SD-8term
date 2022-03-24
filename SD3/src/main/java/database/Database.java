package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Database {
    private final String name;

    private void updateSQL(final String sql) {
        try (final Connection c = DriverManager.getConnection("jdbc:sqlite:" + name + ".db")) {
            final Statement stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (final SQLException e) {
            throw new DatabaseException("Can't update SQL: " + e.getMessage(), e);
        }
    }

    private <R> R querySQL(final String sql, final Function<ResultSet, R> resultSetConsumer) {
        try (final Connection c = DriverManager.getConnection("jdbc:sqlite:" + name + ".db")) {
            final Statement stmt = c.createStatement();
            final ResultSet rs = stmt.executeQuery(sql);
            final R result = resultSetConsumer.apply(rs);
            stmt.close();
            return result;
        } catch (final SQLException e) {
            throw new DatabaseException("Can't execute query: " + e.getMessage(), e);
        }
    }

    private boolean containsMembership(final Membership membership) {
        return containsMembership(membership.getId());
    }

    private boolean containsMembership(final int membershipId) {
        return querySQL("select * from Memberships where Memberships.id = " + membershipId, rs -> {
            try {
                return rs.next();
            } catch (final SQLException ignored) {
                return false;
            }
        });
    }

    private boolean containsMembershipEvent(final MembershipEvent membershipEvent) {
        return querySQL("select * from MembershipEvents" +
                "            where MembershipEvents.eventId    = " + membershipEvent.getEventId() +
                "            and MembershipEvents.membershipId = " + membershipEvent.getMembershipId(), rs -> {
            try {
                return rs.next();
            } catch (final SQLException ignored) {
                return false;
            }
        });
    }

    private boolean containsTurnstileEvent(final TurnstileEvent turnstileEvent) {
        return querySQL("select * from TurnstileEvents where" +
                "            TurnstileEvents.membershipId = " + turnstileEvent.getMembershipId() +
                "            and TurnstileEvents.eventId  = " + turnstileEvent.getEventId(), rs -> {
            try {
                return rs.next();
            } catch (final SQLException ignored) {
                return false;
            }
        });
    }

    private void dropOldTables() {
        updateSQL("drop table if exists Memberships");
        updateSQL("drop table if exists MembershipEvents");
        updateSQL("drop table if exists TurnstileEvents");
    }

    public Database(final String name) {
        this(name, "");
    }

    public Database(final String name, final String parameters) {
        this.name = name;
        if (parameters.equals("--drop-old-tables")) {
            dropOldTables();
        } else if (!parameters.equals("")) {
            throw new DatabaseException("Incorrect parameters string");
        }
        updateSQL("create table if not exists Memberships" +
                "  (id integer primary key not null," +
                "   name text not null," +
                "   addedTime integer not null)");
        updateSQL("create table if not exists MembershipEvents" +
                "  (eventId integer not null," +
                "   membershipId integer not null," +
                "   validTill integer not null," +
                "   addedTime integer not null," +
                "   primary key (eventId, membershipId))");
        updateSQL("create table if not exists TurnstileEvents" +
                "  (eventId integer not null," +
                "   membershipId integer not null," +
                "   event text not null," +
                "   addedTime integer not null," +
                "   primary key (eventId, membershipId))");
    }

    public void addMembership(final Membership membership) {
        if (containsMembership(membership)) {
            throw new DatabaseException("Membership with id = " + membership.getId() + " is already added");
        } else {
            updateSQL("insert into Memberships " +
                    "  (id, name, addedTime) values " +
                    "  (" + membership.getId() + ", '" + membership.getName() + "', " + membership.getAddedTimeInSeconds() + ")");
        }
    }

    public void addMembershipEvent(final MembershipEvent membershipEvent) {
        if (!containsMembership(membershipEvent.getMembershipId())) {
            throw new DatabaseException("Can't find membership with id = " + membershipEvent.getMembershipId());
        } else if (containsMembershipEvent(membershipEvent)) {
            throw new DatabaseException("Membership event " + membershipEvent.getEventId() +
                    " for membership " + membershipEvent.getMembershipId() + " is already added");
        } else {
            updateSQL("insert into MembershipEvents " +
                    "  (eventId, MembershipId, validTill, addedTime) values " +
                    "  (" + membershipEvent.getEventId() +
                    " , " + membershipEvent.getMembershipId() +
                    " , " + membershipEvent.getValidTillInSeconds() +
                    " , " + membershipEvent.getAddedTimeInSeconds() + ")");
        }
    }

    public void addTurnstileEvent(final TurnstileEvent turnstileEvent) {
        if (!containsMembership(turnstileEvent.getMembershipId())) {
            throw new DatabaseException("Can't find membership with id = " + turnstileEvent.getMembershipId());
        } else if (containsTurnstileEvent(turnstileEvent)) {
            throw new DatabaseException("Turnstile event " + turnstileEvent.getEventId() +
                    " for membership " + turnstileEvent.getMembershipId() + " is already added");
        } else {
            updateSQL("insert into TurnstileEvents " +
                    "  (eventId, membershipId, event, addedTime) values " +
                    "  (" + turnstileEvent.getEventId() +
                    " , " + turnstileEvent.getMembershipId() +
                    " ,'" + turnstileEvent.getEvent().toString() + "'" +
                    " , " + turnstileEvent.getAddedTimeInSeconds() + ")");
        }
    }

    public Membership getMembership(final int membershipId) {
        if (!containsMembership(membershipId)) {
            throw new DatabaseException("Can't find membership with id = " + membershipId);
        } else {
            return querySQL("select * from Memberships" +
                    " where Memberships.id = " + membershipId +
                    " order by Memberships.id", rs -> {
                try {
                    return new Membership(rs.getInt("id"), rs.getString("name"), rs.getInt("addedTime"));
                } catch (final SQLException e) {
                    throw new DatabaseException("Can't get membership: " + e.getMessage(), e);
                }
            });
        }
    }

    public List<Membership> getMemberships() {
        return querySQL("select * from Memberships", rs -> {
            final List<Membership> memberships = new ArrayList<>();
            try {
                while (rs.next()) {
                    memberships.add(new Membership(rs.getInt("id"), rs.getString("name"), rs.getInt("addedTime")));
                }
            } catch (final SQLException e) {
                throw new DatabaseException("Can't extract memberships from database: " + e.getMessage(), e);
            }
            return memberships;
        });
    }

    public List<MembershipEvent> getMembershipsEvents(final int membershipId) {
        if (!containsMembership(membershipId)) {
            throw new DatabaseException("Can't find membership with id = " + membershipId);
        } else {
            return querySQL("select * from MembershipEvents" +
                    " where MembershipEvents.membershipId = " + membershipId +
                    " order by MembershipEvents.membershipId, MembershipEvents.eventId", rs -> {
                final List<MembershipEvent> events = new ArrayList<>();
                try {
                    while (rs.next()) {
                        events.add(new MembershipEvent(rs.getInt("eventId"),
                                rs.getInt("membershipId"),
                                rs.getInt("validTill"),
                                rs.getInt("addedTime")));
                    }
                } catch (final SQLException e) {
                    throw new DatabaseException("Can't extract events from database: " + e.getMessage(), e);
                }
                return events;
            });
        }
    }

    public List<TurnstileEvent> getTurnstileEvents(final int membershipId) {
        if (!containsMembership(membershipId)) {
            throw new DatabaseException("Can't find membership with id = " + membershipId);
        } else {
            return querySQL("select * from TurnstileEvents" +
                    " where TurnstileEvents.membershipId = " + membershipId +
                    " order by TurnstileEvents.membershipId, TurnstileEvents.eventId", rs -> {
                final List<TurnstileEvent> events = new ArrayList<>();
                try {
                    while (rs.next()) {
                        events.add(new TurnstileEvent(rs.getInt("eventId"),
                                rs.getInt("membershipId"),
                                TypeOfTurnstileEvent.valueOf(rs.getString("event")),
                                rs.getInt("addedTime")));
                    }
                } catch (final SQLException e) {
                    throw new DatabaseException("Can't extract events from database: " + e.getMessage(), e);
                }
                return events;
            });
        }
    }
}
