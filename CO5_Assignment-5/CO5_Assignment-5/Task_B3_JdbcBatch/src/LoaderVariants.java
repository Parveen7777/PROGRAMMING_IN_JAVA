import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/** The other insertion strategies compared in B3 (all take the same list of passes). */
public class LoaderVariants {
    static final String INS = "INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) VALUES (?, ?, ?, ?, ?)";

    /** (a) Statement, SQL text built per row, auto-commit ON. */
    public static int statementAutoCommit(Connection con, List<String[]> passes) throws SQLException {
        con.setAutoCommit(true);
        int n = 0;
        try (Statement st = con.createStatement()) {
            for (String[] p : passes) {
                st.executeUpdate("INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) VALUES ('"
                        + p[0] + "','" + p[1] + "','" + p[2] + "'," + p[3] + "," + p[4] + ")");
                n++;
            }
        }
        return n;
    }

    /** (b) PreparedStatement, one executeUpdate per row, auto-commit ON. */
    public static int preparedAutoCommit(Connection con, List<String[]> passes) throws SQLException {
        con.setAutoCommit(true);
        int n = 0;
        try (PreparedStatement ps = con.prepareStatement(INS)) {
            for (String[] p : passes) {
                bind(ps, p);
                ps.executeUpdate();
                n++;
            }
        }
        return n;
    }

    /** (c) PreparedStatement + addBatch/executeBatch every 1000 rows, auto-commit ON (no transaction). */
    public static int batchAutoCommit(Connection con, List<String[]> passes) throws SQLException {
        con.setAutoCommit(true);
        int n = 0;
        try (PreparedStatement ps = con.prepareStatement(INS)) {
            for (String[] p : passes) {
                bind(ps, p);
                ps.addBatch();
                if (++n % 1000 == 0) ps.executeBatch();
            }
            ps.executeBatch();
        }
        return n;
    }

    /** (e) Alternative: one multi-row INSERT per 1000 rows inside one transaction. */
    public static int multiRowInsert(Connection con, List<String[]> passes) throws SQLException {
        con.setAutoCommit(false);
        int n = 0;
        try (Statement st = con.createStatement()) {
            StringBuilder sb = new StringBuilder();
            for (String[] p : passes) {
                if (sb.length() == 0)
                    sb.append("INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) VALUES ");
                else sb.append(',');
                sb.append("('").append(p[0]).append("','").append(p[1]).append("','").append(p[2])
                  .append("',").append(p[3]).append(',').append(p[4]).append(')');
                if (++n % 1000 == 0) { st.executeUpdate(sb.toString()); sb.setLength(0); }
            }
            if (sb.length() > 0) st.executeUpdate(sb.toString());
            con.commit();
        } catch (SQLException | RuntimeException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
        return n;
    }

    /** (f) Alternative: MySQL/MariaDB LOAD DATA LOCAL INFILE (server-side bulk loader). */
    public static int loadData(Connection con, List<String[]> passes) throws Exception {
        Path tmp = Files.createTempFile("passes", ".csv");
        try {
            StringBuilder sb = new StringBuilder();
            for (String[] p : passes) sb.append(String.join(",", p)).append('\n');
            Files.writeString(tmp, sb.toString());
            con.setAutoCommit(true);
            try (Statement st = con.createStatement()) {
                String path = tmp.toString().replace("\\", "/");
                return st.executeUpdate("LOAD DATA LOCAL INFILE '" + path + "' INTO TABLE bus_pass "
                        + "FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' "
                        + "(pass_id, reg_no, name, route_no, amount)");
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    static void bind(PreparedStatement ps, String[] p) throws SQLException {
        ps.setString(1, p[0]);
        ps.setString(2, p[1]);
        ps.setString(3, p[2]);
        ps.setInt(4, Integer.parseInt(p[3]));
        ps.setDouble(5, Double.parseDouble(p[4]));
    }
}
