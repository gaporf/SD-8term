package server;

import eshop.Currency;
import eshop.EshopUtils;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import mongo.Mongo;
import rx.Observable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerUtils {
    public static Observable<String> handle(final Mongo mongo, final HttpServerRequest<io.netty.buffer.ByteBuf> request) {
        Observable<String> result;
        try {
            final String path = request.getDecodedPath().substring(1);
            switch (path) {
                case "register_user" -> result = registerUser(mongo, request);
                case "unregister_user" -> result = unregisterUser(mongo, request);
                case "show_user" -> result = showUser(mongo, request);
                case "add_item" -> result = addItem(mongo, request);
                case "remove_item" -> result = removeItem(mongo, request);
                case "show_item" -> result = showItem(mongo, request);
                case "add_item_to_cart" -> result = addItemToCart(mongo, request);
                case "remove_item_from_cart" -> result = removeItemFromCart(mongo, request);
                case "show_cart" -> result = showCart(mongo, request);
                default -> throw new ServerShowException("Incorrect request path");
            }
        } catch (final Exception e) {
            result = Observable.just("Can't handle request: " + e.getMessage());
        }
        return result;
    }

    private static int parseInt(final String stringToParse, final String errorMessage) {
        try {
            return Integer.parseInt(stringToParse);
        } catch (final Exception e) {
            throw new ServerShowException(errorMessage);
        }
    }

    private static double parseDouble(final String stringToParse, final String errorMessage) {
        try {
            return Double.parseDouble(stringToParse);
        } catch (final Exception e) {
            throw new ServerShowException(errorMessage);
        }
    }

    private static Observable<String> registerUser(final Mongo mongo, final HttpServerRequest<io.netty.buffer.ByteBuf> request) {
        final List<String> id = request.getQueryParameters().get("id");
        final List<String> name = request.getQueryParameters().get("name");
        final List<String> currency = request.getQueryParameters().get("currency");
        if (id == null || id.size() != 1
                || name == null || name.size() != 1
                || currency == null || currency.size() != 1
                || request.getQueryParameters().keySet().size() != 3) {
            throw new ServerShowException("Requested pattern is /register_user?id=<id>&name=<name>&currency=<currency>");
        }
        final int idInt = parseInt(id.get(0), "id is not correct int");
        return mongo.registerUser(idInt, name.get(0), EshopUtils.getCurrencyFromString(currency.get(0)));
    }

    private static Observable<String> showUser(final Mongo mongo, final HttpServerRequest<io.netty.buffer.ByteBuf> request) {
        final List<String> id = request.getQueryParameters().get("id");
        if (id == null || id.size() != 1
                || request.getQueryParameters().keySet().size() != 1) {
            throw new ServerShowException("Requested pattern is /show_user?id=<id>");
        }
        final int idInt = parseInt(id.get(0), "id is not correct int");
        return mongo.getUser(idInt);
    }

    private static Observable<String> unregisterUser(final Mongo mongo, final HttpServerRequest<io.netty.buffer.ByteBuf> request) {
        final List<String> id = request.getQueryParameters().get("id");
        if (id == null || id.size() != 1
                || request.getQueryParameters().keySet().size() != 1) {
            throw new ServerShowException(("Requested pattern is /unregister_user?id=<id>"));
        }
        final int idInt = parseInt(id.get(0), "id is not correct int");
        return mongo.unregisterUser(idInt);
    }

    private static Observable<String> addItem(final Mongo mongo, final HttpServerRequest<io.netty.buffer.ByteBuf> request) {
        final List<String> id = request.getQueryParameters().get("id");
        final List<String> name = request.getQueryParameters().get("name");
        if (id == null || id.size() != 1
                || name == null || name.size() != 1) {
            throw new ServerShowException("Requested pattern is /add_item?id=<id>&name=<name>&[price_in_<currency>=<price>]*");
        }
        final int idInt = parseInt(id.get(0), "id is not correct int");
        final Map<Currency, Double> prices = new HashMap<>();
        for (final String price : request.getQueryParameters().keySet()) {
            if (price.startsWith("price_in_")) {
                final String currency = price.substring("price_in_".length());
                prices.put(EshopUtils.getCurrencyFromString(currency),
                        parseDouble(request.getQueryParameters().get(price).get(0), "price is not correct double"));
            } else if (!price.equals("id") && !price.equals("name")) {
                throw new ServerShowException("Requested pattern is /add_item?id=<id>&name=<name>&[price_in_<currency>=<price>]*");
            }
        }
        return mongo.addItem(idInt, name.get(0), prices);
    }

    private static Observable<String> showItem(final Mongo mongo, final HttpServerRequest<io.netty.buffer.ByteBuf> request) {
        final List<String> id = request.getQueryParameters().get("id");
        if (id == null || id.size() != 1
                || request.getQueryParameters().keySet().size() != 1) {
            throw new ServerShowException("Requested pattern is /show_item?id=<id>");
        }
        final int idInt = parseInt(id.get(0), "id is not correct int");
        return mongo.showItem(idInt);
    }

    private static Observable<String> removeItem(final Mongo mongo, final HttpServerRequest<io.netty.buffer.ByteBuf> request) {
        final List<String> id = request.getQueryParameters().get("id");
        if (id == null || id.size() != 1
                || request.getQueryParameters().keySet().size() != 1) {
            throw new ServerShowException("Requested pattern is /remove_item?id=<id>");
        }
        final int idInt = parseInt(id.get(0), "id is not correct int");
        return mongo.removeItem(idInt);
    }

    private static Observable<String> addItemToCart(final Mongo mongo, final HttpServerRequest<io.netty.buffer.ByteBuf> request) {
        final List<String> userId = request.getQueryParameters().get("user_id");
        final List<String> itemId = request.getQueryParameters().get("item_id");
        if (userId == null || userId.size() != 1
                || itemId == null || itemId.size() != 1
                || request.getQueryParameters().keySet().size() != 2) {
            throw new ServerShowException("Requested pattern is /add_item_to_cart?user_id=<user_id>&item_id=<item_id>");
        }
        final int userIdInt = parseInt(userId.get(0), "user id is not correct int");
        final int itemIdInt = parseInt(itemId.get(0), "item id is not correct int");
        return mongo.addItemToCart(userIdInt, itemIdInt);
    }

    private static Observable<String> removeItemFromCart(final Mongo mongo, final HttpServerRequest<io.netty.buffer.ByteBuf> request) {
        final List<String> userId = request.getQueryParameters().get("user_id");
        final List<String> itemId = request.getQueryParameters().get("item_id");
        if (userId == null || userId.size() != 1
                || itemId == null || itemId.size() != 1
                || request.getQueryParameters().keySet().size() != 2) {
            throw new ServerShowException("Requested pattern is /remove_item_from_cart?user_id=<user_id>&item_id=<item_id>");
        }
        final int userIdInt = parseInt(userId.get(0), "user id is not correct int");
        final int itemIdInt = parseInt(itemId.get(0), "item id is not correct int");
        return mongo.removeItemFromCart(userIdInt, itemIdInt);
    }

    private static Observable<String> showCart(final Mongo mongo, final HttpServerRequest<io.netty.buffer.ByteBuf> request) {
        final List<String> userId = request.getQueryParameters().get("user_id");
        if (userId == null || userId.size() != 1
                || request.getQueryParameters().keySet().size() != 1) {
            throw new ServerShowException("Requested pattern is /show_cart?user_id=<user_id>");
        }
        final int userIdInt = parseInt(userId.get(0), "user id is not correct int");
        return mongo.showCart(userIdInt);
    }
}
