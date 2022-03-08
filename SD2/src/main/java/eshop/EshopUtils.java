package eshop;

public class EshopUtils {
    public static Currency getCurrencyFromString(final String string) {
        return switch (string.toLowerCase()) {
            case "rub" -> Currency.RUB;
            case "usd" -> Currency.USD;
            case "eur" -> Currency.EUR;
            default -> throw new EshopException("Unknown currency");
        };
    }

    public static String getStringByCurrency(final Currency currency) {
        return switch (currency) {
            case RUB -> "rub";
            case EUR -> "eur";
            case USD -> "usd";
        };
    }
}
