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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MembershipEvent)) {
            return false;
        } else if (this == obj) {
            return true;
        } else {
            final MembershipEvent another = (MembershipEvent) obj;
            return this.eventId == another.eventId && this.membershipId == another.membershipId &&
                    this.validTillInSeconds == another.validTillInSeconds && this.addedTimeInSeconds == another.addedTimeInSeconds;
        }
    }
}
