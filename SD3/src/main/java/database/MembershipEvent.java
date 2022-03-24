package database;

import clock.Clock;

public class MembershipEvent {
    private final int eventId;
    private final int membershipId;
    private final long validTillInSeconds;
    private final long addedTimeInSeconds;

    public MembershipEvent(final int eventId, final int membershipId, final long validTillInSeconds, final long addedTimeInSeconds) {
        this.eventId = eventId;
        this.membershipId = membershipId;
        this.validTillInSeconds = validTillInSeconds;
        this.addedTimeInSeconds = addedTimeInSeconds;
    }

    public int getEventId() {
        return eventId;
    }

    public int getMembershipId() {
        return membershipId;
    }

    public long getValidTillInSeconds() {
        return validTillInSeconds;
    }

    public long getAddedTimeInSeconds() {
        return addedTimeInSeconds;
    }
}
