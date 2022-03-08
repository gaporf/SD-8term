package mongo;

import com.mongodb.rx.client.*;
import eshop.Currency;
import eshop.EshopUtils;
import eshop.Item;
import eshop.User;
import org.bson.Document;
import rx.Observable;

import java.util.Map;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;


public class Mongo {
    private final MongoClient client;

    public Mongo(final String url) {
        client = MongoClients.create(url);
    }

    public Observable<String> registerUser(final int id, final String name, final Currency currency) {
        final MongoCollection<Document> users = client.getDatabase("eshop").getCollection("users");
        final Document userDocument = new Document("id", id)
                .append("name", name)
                .append("currency", EshopUtils.getStringByCurrency(currency));
        return users.find(eq("id", id)).toObservable().isEmpty().flatMap(isNotFound -> {
            if (isNotFound) {
                return users.insertOne(userDocument).map(suc -> "User was successfully added");
            } else {
                return Observable.just("User has already existed");
            }
        });
    }

    public Observable<String> getUser(final int id) {
        final MongoCollection<Document> users = client.getDatabase("eshop").getCollection("users");
        return users.find(eq("id", id)).toObservable().isEmpty().flatMap(isUserNotFound -> {
            if (isUserNotFound) {
                return Observable.just("User was not found");
            } else {
                return users.find(eq("id", id)).toObservable().map(User::new).map(User::toString);
            }
        });
    }

    public Observable<String> unregisterUser(final int id) {
        final MongoCollection<Document> users = client.getDatabase("eshop").getCollection("users");
        return users.deleteOne(eq("id", id)).asObservable().map(res -> {
            if (res.getDeletedCount() == 0) {
                return "User not found";
            } else {
                return "User was successfully unregister";
            }
        });
    }

    public Observable<String> addItem(final int id, final String name, final Map<Currency, Double> prices) {
        final MongoCollection<Document> items = client.getDatabase("eshop").getCollection("items");
        final Document itemDocument = new Document("id", id)
                .append("name", name);
        for (Currency currency : prices.keySet()) {
            itemDocument.append("price_in_" + EshopUtils.getStringByCurrency(currency), prices.get(currency));
        }
        return items.find(eq("id", id)).toObservable().isEmpty().flatMap(isNotFound -> {
            if (isNotFound) {
                return items.insertOne(itemDocument).map(suc -> "Item was successfully added");
            } else {
                return Observable.just("Item has already existed");
            }
        });
    }

    public Observable<String> showItem(final int id) {
        final MongoCollection<Document> items = client.getDatabase("eshop").getCollection("items");
        return items.find(eq("id", id)).toObservable().isEmpty().flatMap(isItemNotFound -> {
            if (isItemNotFound) {
                return Observable.just("Item was not found");
            } else {
                return items.find(eq("id", id)).toObservable().map(Item::new).map(Item::toString);
            }
        });
    }

    public Observable<String> removeItem(final int id) {
        final MongoCollection<Document> items = client.getDatabase("eshop").getCollection("items");
        return items.deleteOne(eq("id", id)).asObservable().map(res -> {
            if (res.getDeletedCount() == 0) {
                return "Item was not found";
            } else {
                return "Item was successfully removed";
            }
        });
    }

    public Observable<String> addItemToCart(final int userId, final int itemId) {
        final MongoCollection<Document> users = client.getDatabase("eshop").getCollection("users");
        final MongoCollection<Document> items = client.getDatabase("eshop").getCollection("items");
        final MongoCollection<Document> carts = client.getDatabase("eshop").getCollection("carts");
        final Document cartDocument = new Document("user_id", userId)
                .append("item_id", itemId);
        return users.find(eq("id", userId)).toObservable().isEmpty().flatMap(isUserNotFound -> {
            if (isUserNotFound) {
                return Observable.just("User was not found");
            } else {
                return items.find(eq("id", itemId)).toObservable().isEmpty().flatMap(isItemNotFound -> {
                    if (isItemNotFound) {
                        return Observable.just("Item was not found");
                    } else {
                        return users.find(eq("id", userId)).toObservable().flatMap(user -> {
                            final Currency currency = EshopUtils.getCurrencyFromString(user.getString("currency"));
                            return items.find(eq("id", itemId)).toObservable().flatMap(findResult -> {
                                if (!findResult.containsKey("price_in_" + EshopUtils.getStringByCurrency(currency))) {
                                    return Observable.just("Item is unavailable for purchase for this user");
                                } else {
                                    return carts.insertOne(cartDocument).map(suc -> "Item was added to the cart");
                                }
                            });
                        });
                    }
                });
            }
        });
    }

    public Observable<String> removeItemFromCart(final int userId, final int itemId) {
        final MongoCollection<Document> users = client.getDatabase("eshop").getCollection("users");
        final MongoCollection<Document> items = client.getDatabase("eshop").getCollection("items");
        final MongoCollection<Document> carts = client.getDatabase("eshop").getCollection("carts");
        return users.find(eq("id", userId)).toObservable().isEmpty().flatMap(isUserNotFound -> {
            if (isUserNotFound) {
                return Observable.just("User was not found");
            } else {
                return items.find(eq("id", itemId)).toObservable().isEmpty().flatMap(isItemNotFound -> {
                    if (isItemNotFound) {
                        return Observable.just("Item was not found");
                    } else {
                        return carts.deleteOne(and(eq("user_id", userId), eq("item_id", itemId))).map(deleteResult -> {
                            if (deleteResult.getDeletedCount() == 0) {
                                return "Item was not found in cart";
                            } else {
                                return "Item was successfully removed";
                            }
                        });
                    }
                });
            }
        });
    }

    public Observable<String> showCart(final int userId) {
        final MongoCollection<Document> users = client.getDatabase("eshop").getCollection("users");
        final MongoCollection<Document> items = client.getDatabase("eshop").getCollection("items");
        final MongoCollection<Document> carts = client.getDatabase("eshop").getCollection("carts");
        return users.find(eq("id", userId)).toObservable().isEmpty().flatMap(isUserNotFound -> {
            if (isUserNotFound) {
                return Observable.just("User was not found");
            } else {
                return carts.find(eq("user_id", userId)).toObservable().flatMap(findResult ->
                        items.find(eq("id", findResult.getInteger("item_id"))).toObservable().map(Item::new).map(Item::toString));
            }
        });
    }
}