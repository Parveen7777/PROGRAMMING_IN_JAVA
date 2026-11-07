import java.math.BigDecimal;
import java.sql.SQLException;

/** Data-access abstraction. The service does not know whether JDBC or memory is behind it. */
public interface FeeRepository {
    void save(String regNo, BigDecimal amount) throws SQLException;
}
