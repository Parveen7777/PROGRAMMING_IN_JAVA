import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Pattern;

/** B2 alternative concept - cost of regex validation / escaping vs PreparedStatement per query. */
public class ValidationCostBenchmark {
    static final Pattern SAFE = Pattern.compile("[A-Za-z0-9_@.#!$-]{1,30}");

    static String escape(String s) { return s.replace("\\", "\\\\").replace("'", "''"); }

    public static void main(String[] args) throws Exception {
        int N = 20000;
        try (Connection con = DbConfig.connect()) {
            // pure CPU cost of validation / escaping (no database)
            long t = System.nanoTime();
            int ok = 0;
            for (int i = 0; i < N; i++) if (SAFE.matcher("alice" + (i % 10)).matches()) ok++;
            double regexUs = (System.nanoTime() - t) / 1e3 / N;
            t = System.nanoTime();
            int len = 0;
            for (int i = 0; i < N; i++) len += escape("o'brien" + i).length();
            double escUs = (System.nanoTime() - t) / 1e3 / N;
            System.out.printf("Regex validation CPU cost : %.3f microseconds per input (ok=%d)%n", regexUs, ok);
            System.out.printf("Quote escaping CPU cost   : %.3f microseconds per input (len=%d)%n", escUs, len);

            // end-to-end query cost: concatenated Statement vs PreparedStatement
            for (int w = 0; w < 2000; w++) {   // warm-up
                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery("SELECT * FROM users WHERE id='alice' AND pwd='x'")) { rs.next(); }
            }
            t = System.nanoTime();
            for (int i = 0; i < N; i++) {
                String id = "alice";
                if (!SAFE.matcher(id).matches()) continue;
                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery("SELECT * FROM users WHERE id='" + id + "' AND pwd='x'")) { rs.next(); }
            }
            double stmt = (System.nanoTime() - t) / 1e6;

            t = System.nanoTime();
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE id=? AND pwd=?")) {
                for (int i = 0; i < N; i++) {
                    ps.setString(1, "alice");
                    ps.setString(2, "x");
                    try (ResultSet rs = ps.executeQuery()) { rs.next(); }
                }
            }
            double prep = (System.nanoTime() - t) / 1e6;
            System.out.printf("%d queries: validated+concatenated Statement = %.0f ms, PreparedStatement (reused) = %.0f ms%n",
                    N, stmt, prep);
        }
    }
}
