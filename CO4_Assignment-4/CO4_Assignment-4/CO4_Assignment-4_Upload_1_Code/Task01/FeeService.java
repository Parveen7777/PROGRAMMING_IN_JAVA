import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/** Application logic: validation + payment. No AWT/Swing import - therefore unit-testable. */
public class FeeService {
    private static final Pattern REG_NO = Pattern.compile("[A-Za-z0-9]{6,15}");
    private static final BigDecimal MAX = new BigDecimal("500000");
    private final FeeRepository repo;
    private final BankGateway bank;
    private final AtomicInteger seq = new AtomicInteger(1000);

    public FeeService(FeeRepository repo, BankGateway bank) { this.repo = repo; this.bank = bank; }

    /** @return null when valid, otherwise a user-readable error message. */
    public String validate(String regNo, String amountText) {
        if (regNo == null || regNo.isBlank()) return "Register number is required.";
        if (!REG_NO.matcher(regNo.trim()).matches()) return "Register number must be 6-15 letters/digits.";
        if (amountText == null || amountText.isBlank()) return "Amount is required.";
        BigDecimal a;
        try { a = new BigDecimal(amountText.trim()); }
        catch (NumberFormatException ex) { return "Amount must be a number."; }
        if (a.signum() <= 0) return "Amount must be greater than zero.";
        if (a.scale() > 2) return "Amount can have at most 2 decimal places.";
        if (a.compareTo(MAX) > 0) return "Amount cannot exceed 5,00,000.";
        return null;
    }

    /** Validates, asks the bank, stores the payment. Slow - call from a worker thread. */
    public PaymentResult pay(String regNo, String amountText) {
        String err = validate(regNo, amountText);
        if (err != null) return PaymentResult.fail(err);
        BigDecimal amount = new BigDecimal(amountText.trim());
        try {
            if (!bank.confirm(regNo.trim(), amount)) return PaymentResult.fail("Bank declined the payment.");
            repo.save(regNo.trim(), amount);
            String no = "RC" + seq.incrementAndGet();
            return PaymentResult.ok(no, "Payment of Rs. " + amount + " recorded. Receipt " + no);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return PaymentResult.fail("Payment was interrupted.");
        } catch (SQLException ex) {
            return PaymentResult.fail("Database error - payment not saved. Please retry.");
        }
    }
}
