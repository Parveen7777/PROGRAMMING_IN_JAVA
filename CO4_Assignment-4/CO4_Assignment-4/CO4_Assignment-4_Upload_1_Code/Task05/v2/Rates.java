import java.util.Map;

/** Shared conversion logic (pure Java - no GUI). 1 unit of currency expressed in INR. */
public final class Rates {
    public static final String[] CURRENCIES = {"INR", "USD", "EUR"};
    private static final Map<String, Double> INR_PER_UNIT = Map.of("INR", 1.0, "USD", 1 / 0.012, "EUR", 1 / 0.011);
    private Rates() {}
    public static double convert(String from, String to, double amount) {
        return amount * INR_PER_UNIT.get(from) / INR_PER_UNIT.get(to);
    }
}
