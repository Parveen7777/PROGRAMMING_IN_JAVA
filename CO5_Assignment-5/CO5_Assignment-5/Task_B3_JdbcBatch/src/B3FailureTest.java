import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/** B3.3 - force a failure half-way and compare auto-commit with a single transaction. */
public class B3FailureTest {
    static long count(Connection con) throws Exception {
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM bus_pass")) {
            rs.next();
            return rs.getLong(1);
        }
    }
    static void clear(Connection con) throws Exception {
        try (Statement st = con.createStatement()) { st.execute("TRUNCATE TABLE bus_pass"); }
    }

    public static void main(String[] args) throws Exception {
        int n = 10000;
        try (Connection con = DbConfig.connect()) {
            // Failure type 1: an invalid amount ("12x.5") at position 5000 -> NumberFormatException in the loop
            List<String[]> bad = PassGenerator.generate(n);
            bad.get(n / 2)[4] = "12x.5";

            clear(con);
            try {
                LoaderVariants.batchAutoCommit(con, bad);
            } catch (Exception e) {
                System.out.println("[AUTO-COMMIT batches]   failed with: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            }
            System.out.println("[AUTO-COMMIT batches]   rows left in table after failure = " + count(con)
                    + "   (half-loaded: DATA IS INCONSISTENT)");

            clear(con);
            try {
                PassLoader.loadAll(con, bad);
            } catch (Exception e) {
                System.out.println("[ONE TRANSACTION]       failed with: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            }
            System.out.println("[ONE TRANSACTION]       rows left in table after failure = " + count(con)
                    + "   (rolled back: ALL-OR-NOTHING)");

            // Failure type 2: duplicate primary key at position 7500 -> SQLException raised by the database itself
            List<String[]> dup = PassGenerator.generate(n);
            dup.get(7500)[0] = dup.get(10)[0];

            clear(con);
            try {
                LoaderVariants.batchAutoCommit(con, dup);
            } catch (Exception e) {
                System.out.println("\n[AUTO-COMMIT batches]   duplicate key -> " + e.getClass().getSimpleName());
            }
            System.out.println("[AUTO-COMMIT batches]   rows left = " + count(con));

            clear(con);
            try {
                PassLoader.loadAll(con, dup);
            } catch (Exception e) {
                System.out.println("[ONE TRANSACTION]       duplicate key -> " + e.getClass().getSimpleName());
            }
            System.out.println("[ONE TRANSACTION]       rows left = " + count(con));

            // Control: valid data commits fully
            clear(con);
            int ok = PassLoader.loadAll(con, PassGenerator.generate(n));
            System.out.println("\n[CONTROL] valid data via PassLoader.loadAll -> loaded=" + ok + ", rows in table=" + count(con));
            clear(con);
        }
    }
}
