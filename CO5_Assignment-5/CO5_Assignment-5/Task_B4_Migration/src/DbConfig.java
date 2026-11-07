import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Connection settings. Override with environment variables DB_URL, DB_USER, DB_PASS. */
public class DbConfig {
    public static String url() {
        return System.getenv().getOrDefault("DB_URL",
                "jdbc:mysql://localhost:3306/transport_test");
    }
    public static String user() { return System.getenv().getOrDefault("DB_USER", "root"); }
    public static String pass() { return System.getenv().getOrDefault("DB_PASS", "root"); }

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(url(), user(), pass());
    }
}
