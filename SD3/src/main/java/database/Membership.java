package database;

import clock.Clock;

public class Membership {
    private final int id;
    private final String name;
    private final long addedTimeInSeconds;

    public Membership(final int id, final String name, final long addedTimeInSeconds) {
        this.id = id;
        this.name = name;
        this.addedTimeInSeconds = addedTimeInSeconds;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getAddedTimeInSeconds() {
        return addedTimeInSeconds;
    }
}
