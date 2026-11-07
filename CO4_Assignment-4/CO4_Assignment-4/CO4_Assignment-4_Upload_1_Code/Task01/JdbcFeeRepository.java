import java.math.BigDecimal;
import java.sql.*;

/** JDBC implementation: PreparedStatement (no SQL injection) + try-with-resources (no leaks). */
public class JdbcFeeRepository implements FeeRepository {
    private final String url, user, pwd;
    public JdbcFeeRepository(String url, String user, String pwd) { this.url = url; this.user = user; this.pwd = pwd; }

    @Override public void save(String regNo, BigDecimal amount) throws SQLException {
        try (Connection c = DriverManager.getConnection(url, user, pwd);
             PreparedStatement ps = c.prepareStatement("INSERT INTO fees(reg_no, amount) VALUES (?, ?)")) {
            ps.setString(1, regNo);
            ps.setBigDecimal(2, amount);
            ps.executeUpdate();
        }
    }
}
