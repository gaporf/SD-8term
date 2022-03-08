package eshop;

import org.bson.Document;

public class User {
    final private int id;
    final private String name;
    final private Currency currency;

    public User(final Document document) {
        this(document.getInteger("id"),
                document.getString("name"),
                EshopUtils.getCurrencyFromString(document.getString("currency")));
    }

    public User(final int id, final String name, final Currency currency) {
        this.id = id;
        this.name = name;
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + "'" +
                ", currency='" + currency + "'" +
                "}" + System.lineSeparator();
    }
}
