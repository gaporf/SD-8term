package database;

import clock.Clock;

public class TurnstileEvent {
    final int eventId;
    final int membershipId;
    final TypeOfTurnstileEvent event;
    final long addedTimeInSeconds;

    public TurnstileEvent(final int eventId, final int membershipId, final TypeOfTurnstileEvent event, final long addedTimeInSeconds) {
        this.eventId = eventId;
        this.membershipId = membershipId;
        this.event = event;
        this.addedTimeInSeconds = addedTimeInSeconds;
    }

    public int getEventId() {
        return eventId;
    }

    public int getMembershipId() {
        return membershipId;
    }

    public TypeOfTurnstileEvent getEvent() {
        return event;
    }

    public long getAddedTimeInSeconds() {
        return addedTimeInSeconds;
    }
}
