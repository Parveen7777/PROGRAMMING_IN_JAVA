import java.math.BigDecimal;

/** Slow external call (bank confirmation). Always called from a background thread. */
public interface BankGateway {
    boolean confirm(String regNo, BigDecimal amount) throws InterruptedException;

    /** Simulated bank: waits to mimic network latency. */
    static BankGateway simulated(long millis) {
        return (r, a) -> { Thread.sleep(millis); return true; };
    }
}
