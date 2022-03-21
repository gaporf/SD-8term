package database;

public class DataBaseMembershipEvent {
    private final int eventId;
    private final int membershipId;
    private final int validTill;
    private final int addedTime;

    public DataBaseMembershipEvent(final int eventId, final int membershipId, final int validTill) {
        this(eventId, membershipId, validTill, (int) (System.currentTimeMillis() / 1000));
    }

    public DataBaseMembershipEvent(final int eventId, final int membershipId, final int validTill, final int addedTime) {
        this.eventId = eventId;
        this.membershipId = membershipId;
        this.validTill = validTill;
        this.addedTime = addedTime;
    }

    public int getEventId() {
        return eventId;
    }

    public int getMembershipId() {
        return membershipId;
    }

    public int getValidTill() {
        return validTill;
    }

    public int getAddedTime() {
        return addedTime;
    }
}
