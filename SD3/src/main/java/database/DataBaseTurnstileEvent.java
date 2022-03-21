package database;

public class DataBaseTurnstileEvent {
    final int eventId;
    final int membershipId;
    final TurnstileEvent event;
    final int addedTime;

    public DataBaseTurnstileEvent(final int eventId, final int membershipId, final TurnstileEvent event) {
        this(eventId, membershipId, event, (int) (System.currentTimeMillis() / 1000));
    }

    public DataBaseTurnstileEvent(final int eventId, final int membershipId, final TurnstileEvent event, final int addedTime) {
        this.eventId = eventId;
        this.membershipId = membershipId;
        this.event = event;
        this.addedTime = addedTime;
    }

    public int getEventId() {
        return eventId;
    }

    public int getMembershipId() {
        return membershipId;
    }

    public TurnstileEvent getEvent() {
        return event;
    }

    public int getAddedTime() {
        return addedTime;
    }
}
