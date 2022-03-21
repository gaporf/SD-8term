package database;

public class DataBaseMembership {
    private final int id;
    private final String name;
    private final int addedTime;

    public DataBaseMembership(final int id, final String name) {
        this(id, name, (int) (System.currentTimeMillis() / 1000));
    }

    public DataBaseMembership(final int id, final String name, final int addedTime) {
        this.id = id;
        this.name = name;
        this.addedTime = addedTime;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAddedTime() {
        return addedTime;
    }
}
