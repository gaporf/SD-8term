package eshop;

import org.bson.Document;

import java.util.HashMap;
import java.util.Map;

public class Item {
    final private int id;
    final private String name;
    final private Map<Currency, Double> prices;

    private static Map<Currency, Double> getPrices(final Document document) {
        final Map<Currency, Double> prices = new HashMap<>();
        for (Currency currency : Currency.values()) {
            if (document.containsKey("price_in_" + EshopUtils.getStringByCurrency(currency))) {
                final Double price = document.getDouble("price_in_" + EshopUtils.getStringByCurrency(currency));
                prices.put(currency, price);
            }
        }
        return prices;
    }

    public Item(final Document document) {
        this(document.getInteger("id"),
                document.getString("name"),
                getPrices(document));
    }

    public Item(final int id, final String name, final Map<Currency, Double> prices) {
        this.id = id;
        this.name = name;
        this.prices = prices;
    }

    @Override
    public String toString() {
        final StringBuilder itemString = new StringBuilder();
        itemString.append("Item{").append("id=").append(id).append(", name=").append(name).append(", ");
        for (Currency currency : prices.keySet()) {
            itemString.append("price in ").append(EshopUtils.getStringByCurrency(currency)).append(" = ").append(prices.get(currency)).append(", ");
        }
        itemString.delete(itemString.length() - 2, itemString.length());
        itemString.append("}").append(System.lineSeparator());
        return itemString.toString();
    }
}
