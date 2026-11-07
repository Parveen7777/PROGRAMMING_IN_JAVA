import java.math.BigDecimal;

/** Plain-Java test (no JUnit needed): proves the logic can be tested without opening a window. */
public class FeeServiceTest {
    static int failed = 0;
    static void check(String name, boolean cond) { System.out.println((cond ? "PASS  " : "FAIL  ") + name); if (!cond) failed++; }

    public static void main(String[] args) {
        InMemoryFeeRepository repo = new InMemoryFeeRepository();
        FeeService s = new FeeService(repo, BankGateway.simulated(0));
        check("empty register number rejected", s.validate("", "100") != null);
        check("short register number rejected", s.validate("12", "100") != null);
        check("non-numeric amount rejected", s.validate("192211001", "abc") != null);
        check("negative amount rejected", s.validate("192211001", "-5") != null);
        check("3 decimal places rejected", s.validate("192211001", "10.123") != null);
        check("huge amount rejected", s.validate("192211001", "9999999") != null);
        check("valid input accepted", s.validate("192211001", "25000.50") == null);
        PaymentResult ok = s.pay("192211001", "25000.50");
        check("payment succeeds", ok.success() && ok.receiptNo() != null);
        check("row stored once", repo.rows.size() == 1);
        check("SQL-injection text rejected, nothing stored", !s.pay("x'); DROP TABLE fees;--", "10").success() && repo.rows.size() == 1);
        BankGateway declines = (r, a) -> false;
        check("declined payment not stored", !new FeeService(repo, declines).pay("192211001", "10").success() && repo.rows.size() == 1);
        System.out.println(failed == 0 ? "ALL TESTS PASSED" : failed + " TEST(S) FAILED");
    }
}
