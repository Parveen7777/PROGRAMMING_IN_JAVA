import java.io.BufferedReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * B4.1 - migrate the legacy text file into the bus_pass table.
 * Valid rows are loaded in batches of 1000 inside ONE transaction; every rejected
 * line is written to a failure report together with the reason.
 */
public class MigrationApp {
    static String dec(String s) {
        return s.replace("%2C", ",").replace("%0A", "\n").replace("%0D", "\r").replace("%25", "%");
    }

    /** Returns null if valid, otherwise the reason for rejection. */
    static String validate(String line, String[] f, Set<String> seen) {
        if (line.isEmpty()) return "empty line";
        String[] p = line.split(",", -1);
        if (p.length != 5) return "wrong number of fields (" + p.length + ")";
        if (p[0].isEmpty() || p[0].length() > 20) return "missing or too long pass id";
        if (p[1].isEmpty() || p[1].length() > 15) return "missing or too long register number";
        if (p[2].isEmpty()) return "missing name";
        int route;
        try { route = Integer.parseInt(p[3]); } catch (NumberFormatException e) { return "route is not a number"; }
        if (route < 1 || route > 99) return "route out of range 1-99";
        double amount;
        try { amount = Double.parseDouble(p[4]); } catch (NumberFormatException e) { return "amount is not a number"; }
        if (!(amount > 0)) return "amount must be positive";
        if (!seen.add(p[0])) return "duplicate pass id in file";
        System.arraycopy(p, 0, f, 0, 5);
        f[2] = dec(f[2]);
        return null;
    }

    public static void main(String[] args) throws Exception {
        Path in = Path.of(args.length > 0 ? args[0] : "legacy_passes.csv");
        Files.createDirectories(Path.of("results"));
        Map<String, Integer> reasons = new LinkedHashMap<>();
        Set<String> seen = new HashSet<>();
        int read = 0, loaded = 0, rejected = 0;
        long t0 = System.nanoTime();

        try (Connection con = DbConfig.connect();
             BufferedReader r = Files.newBufferedReader(in);
             PrintWriter rep = new PrintWriter("results/rejects_report.txt")) {

            rep.println("line_no | reason | raw_line");
            String sql = "INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) VALUES (?,?,?,?,?)";
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                String line;
                int pending = 0;
                String[] f = new String[5];
                while ((line = r.readLine()) != null) {
                    read++;
                    String why = validate(line, f, seen);
                    if (why != null) {
                        rejected++;
                        reasons.merge(why, 1, Integer::sum);
                        rep.println(read + " | " + why + " | " + line);
                        continue;
                    }
                    ps.setString(1, f[0]); ps.setString(2, f[1]); ps.setString(3, f[2]);
                    ps.setInt(4, Integer.parseInt(f[3])); ps.setDouble(5, Double.parseDouble(f[4]));
                    ps.addBatch();
                    loaded++;
                    if (++pending == 1000) { ps.executeBatch(); pending = 0; }
                }
                ps.executeBatch();
                con.commit();                       // all-or-nothing for the valid rows
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                System.out.println("MIGRATION FAILED - transaction rolled back: " + e.getMessage());
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
        double sec = (System.nanoTime() - t0) / 1e9;
        System.out.println("=== MIGRATION REPORT ===");
        System.out.printf("Lines read      : %d%nLoaded rows     : %d%nRejected lines  : %d%nElapsed         : %.2f s%n",
                read, loaded, rejected, sec);
        System.out.println("Rejected by reason:");
        reasons.forEach((k, v) -> System.out.printf("   %-34s %d%n", k, v));
        System.out.println("Details written to results/rejects_report.txt");
    }
}
