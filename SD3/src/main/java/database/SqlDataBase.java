package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SqlDataBase {
    final String name;

    private void updateSQL(final String sql) {
        try (final Connection c = DriverManager.getConnection("jdbc:sqlite:" + name + ".db")) {
            final Statement stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (final SQLException e) {
            throw new SqlDataBaseException("Can't update SQL", e);
        }
    }

    private <R> R querySQL(final String sql, final Function<ResultSet, R> resultSetConsumer) {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + name + ".db")) {
            final Statement stmt = c.createStatement();
            final ResultSet rs = stmt.executeQuery(sql);
            final R result = resultSetConsumer.apply(rs);
            stmt.close();
            return result;
        } catch (final SQLException exception) {
            throw new SqlDataBaseException("Can't execute query", exception);
        }
    }

    private boolean containsMembership(final DataBaseMembership membership) {
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

    private boolean containsMembershipEvent(final DataBaseMembershipEvent membershipEvent) {
        return querySQL("select * from MembershipEvents" +
                " where MembershipEvents.eventId = " + membershipEvent.getEventId() +
                " and MembershipEvents.membershipId = " + membershipEvent.getMembershipId(), rs -> {
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

    public SqlDataBase(final String name) {
        this(name, "");
    }

    public SqlDataBase(final String name, final String parameters) {
        this.name = name;
        if (parameters.equals("--drop-old-tables")) {
            dropOldTables();
        } else if (!parameters.equals("")) {
            throw new SqlDataBaseException("Incorrect parameters string");
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
                "   eventTime integer not null," +
                "   primary key (eventId, membershipId))");
    }

    public void addMembership(final DataBaseMembership membership) {
        if (containsMembership(membership)) {
            throw new SqlDataBaseException("Membership with id = " + membership.getId() + " has already been added");
        } else {
            updateSQL("insert into Memberships " +
                    "  (id, name, addedTime) values " +
                    "  (" + membership.getId() + ", '" + membership.getName() + "', " + membership.getAddedTime() + ")");
        }
    }

    public void addMembershipEvent(final DataBaseMembershipEvent membershipEvent) {
        if (!containsMembership(membershipEvent.getMembershipId())) {
            throw new SqlDataBaseException("Can't find membership with id = " + membershipEvent.getMembershipId());
        } else if (containsMembershipEvent(membershipEvent)) {
            throw new SqlDataBaseException("Membership event with id = " + membershipEvent.getEventId() + " for membership with id = " + membershipEvent.getMembershipId() + " has already been added");
        } else {
            updateSQL("insert into MembershipEvents " +
                    "  (eventId, MembershipId, validTill, addedTime) values " +
                    "  (" + membershipEvent.getEventId() + ", " + membershipEvent.getMembershipId() + ", " + membershipEvent.getValidTill() + ", " + membershipEvent.getAddedTime() + ")");
        }
    }

    public DataBaseMembership getMembership(final int membershipId) {
        if (!containsMembership(membershipId)) {
            throw new SqlDataBaseException("Can't find membership with id = " + membershipId);
        } else {
            return querySQL("select * from Memberships where Memberships.id = " + membershipId, rs -> {
                try {
                    return new DataBaseMembership(rs.getInt("id"), rs.getString("name"), rs.getInt("addedTime"));
                } catch (final SQLException e) {
                    throw new SqlDataBaseException("Can't get membership", e);
                }
            });
        }
    }

    public List<DataBaseMembershipEvent> getMembershipsEvents(final int membershipId) {
        if (!containsMembership(membershipId)) {
            throw new SqlDataBaseException("Can't find membership with id = " + membershipId);
        } else {
            return querySQL("select * from MembershipEvents where MembershipEvents.membershipId = " + membershipId, rs -> {
                final List<DataBaseMembershipEvent> events = new ArrayList<>();
                try {
                    while (rs.next()) {
                        events.add(new DataBaseMembershipEvent(rs.getInt("eventId"), rs.getInt("membershipId"), rs.getInt("validTill"), rs.getInt("addedTime")));
                    }
                } catch (final SQLException e) {
                    throw new SqlDataBaseException("Can't extract events from database", e);
                }
                return events;
            });
        }
    }
}
