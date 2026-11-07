import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** B2 - unsafe (string concatenation) login vs safe (PreparedStatement) login. */
public class LoginDemo {

    /** UNSAFE: user input is pasted into the SQL text. */
    static boolean unsafeLogin(Connection con, String id, String pwd) {
        String sql = "SELECT * FROM users WHERE id='" + id + "' AND pwd='" + pwd + "'";
        System.out.println("   SQL sent  : " + sql);
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next();
        } catch (SQLException e) {
            System.out.println("   SQL error : " + e.getMessage());
            return false;
        }
    }

    /** SAFE: input is bound as DATA to ? placeholders, never parsed as SQL. */
    static boolean safeLogin(Connection con, String id, String pwd) throws SQLException {
        String sql = "SELECT * FROM users WHERE id=? AND pwd=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, pwd);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    static void attempt(Connection con, String label, String id, String pwd) throws SQLException {
        System.out.println("\n[" + label + "]  id=<" + id + ">  pwd=<" + pwd + ">");
        boolean u = unsafeLogin(con, id, pwd);
        boolean s = safeLogin(con, id, pwd);
        System.out.println("   UNSAFE version -> " + (u ? "LOGIN ACCEPTED" : "login rejected"));
        System.out.println("   SAFE   version -> " + (s ? "LOGIN ACCEPTED" : "login rejected"));
    }

    public static void main(String[] args) throws SQLException {
        try (Connection con = DbConfig.connect()) {
            try (Statement st = con.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS users (id VARCHAR(30) PRIMARY KEY, pwd VARCHAR(60) NOT NULL)");
                st.execute("DELETE FROM users");
                st.execute("INSERT INTO users VALUES ('alice','S3cret#2025'), ('admin','Adm!n#9071')");
            }
            System.out.println("Test table users holds: alice / admin (passwords known only to the table)");

            attempt(con, "T1 valid credentials",           "alice", "S3cret#2025");
            attempt(con, "T2 wrong password",              "alice", "guess123");
            attempt(con, "T3 INJECTION: always-true OR",   "alice", "x' OR '1'='1");
            attempt(con, "T4 INJECTION: comment-out pwd",  "admin'-- ", "anything");
            attempt(con, "T5 INJECTION: no id, no pwd",    "' OR 1=1 -- ", "");
            attempt(con, "T6 name with apostrophe (legit)", "o'brien", "pw");
        }
    }
}
