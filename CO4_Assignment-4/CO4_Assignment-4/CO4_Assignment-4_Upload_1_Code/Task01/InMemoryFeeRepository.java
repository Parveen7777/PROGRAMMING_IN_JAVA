import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Test double / demo repository - lets the window and the tests run without MySQL. */
public class InMemoryFeeRepository implements FeeRepository {
    public final List<String> rows = new ArrayList<>();
    @Override public synchronized void save(String regNo, BigDecimal amount) { rows.add(regNo + ":" + amount); }
}
