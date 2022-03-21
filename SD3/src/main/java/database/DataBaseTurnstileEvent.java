package database;

public class DataBaseTurnstileEvent {
    final int eventId;
    final int membershipId;
    final TurnstileEvent event;
    final int timestamp;

    public DataBaseTurnstileEvent(final int eventId, final int membershipId, final TurnstileEvent event, final int timestamp) {
        this.eventId = eventId;
        this.membershipId = membershipId;
        this.event = event;
        this.timestamp = timestamp;
    }
}
