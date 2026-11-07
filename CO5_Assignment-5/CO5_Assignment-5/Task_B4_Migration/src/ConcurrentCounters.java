import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** B4.3 evidence - several counters issuing passes at the same time, and indexed search at scale. */
public class ConcurrentCounters {
    static final String INS = "INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) VALUES (?,?,?,?,?)";

    static long count(Connection con) throws SQLException {
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM bus_pass")) {
            rs.next();
            return rs.getLong(1);
        }
    }

    public static void main(String[] args) throws Exception {
        int counters = 4, perCounter = 2500;
        try (Connection admin = DbConfig.connect()) {
            try (Statement st = admin.createStatement()) { st.execute("TRUNCATE TABLE bus_pass"); }

            // 1. Four counters (threads, each with its OWN connection) issue passes simultaneously
            List<Thread> ts = new ArrayList<>();
            AtomicInteger failures = new AtomicInteger();
            long t = System.nanoTime();
            for (int c = 0; c < counters; c++) {
                final int cid = c;
                Thread th = new Thread(() -> {
                    try (Connection con = DbConfig.connect(); PreparedStatement ps = con.prepareStatement(INS)) {
                        for (int i = 1; i <= perCounter; i++) {
                            ps.setString(1, String.format("C%d-%06d", cid, i));
                            ps.setString(2, "19231" + String.format("%04d", i));
                            ps.setString(3, "Counter" + cid + " student " + i);
                            ps.setInt(4, 1 + i % 30);
                            ps.setDouble(5, 4000.0);
                            ps.executeUpdate();        // each pass is its own committed transaction (a counter sale)
                        }
                    } catch (SQLException e) {
                        failures.incrementAndGet();
                        e.printStackTrace();
                    }
                });
                ts.add(th);
                th.start();
            }
            for (Thread th : ts) th.join();
            double ms = (System.nanoTime() - t) / 1e6;
            System.out.printf("1. %d counters x %d passes issued concurrently in %.0f ms; thread failures=%d%n",
                    counters, perCounter, ms, failures.get());
            System.out.printf("   expected rows = %d, actual rows = %d  -> %s%n", counters * perCounter, count(admin),
                    count(admin) == counters * perCounter ? "NO LOST OR DUPLICATED ROWS" : "MISMATCH");

            // 2. Two counters try to issue the SAME pass id at the same moment: the primary key allows only one
            AtomicInteger won = new AtomicInteger(), lost = new AtomicInteger();
            Thread[] race = new Thread[2];
            java.util.concurrent.CyclicBarrier gate = new java.util.concurrent.CyclicBarrier(2);
            for (int k = 0; k < 2; k++) {
                final int who = k;
                race[k] = new Thread(() -> {
                    try (Connection con = DbConfig.connect(); PreparedStatement ps = con.prepareStatement(INS)) {
                        ps.setString(1, "P-RACE-1"); ps.setString(2, "192319999"); ps.setString(3, "Counter " + who);
                        ps.setInt(4, 5); ps.setDouble(5, 4000.0);
                        gate.await();
                        ps.executeUpdate();
                        won.incrementAndGet();
                    } catch (SQLIntegrityConstraintViolationException e) {
                        lost.incrementAndGet();
                    } catch (Exception e) {
                        if (e.getClass().getSimpleName().contains("Duplicate") || String.valueOf(e.getMessage()).contains("Duplicate"))
                            lost.incrementAndGet();
                        else e.printStackTrace();
                    }
                });
                race[k].start();
            }
            for (Thread th : race) th.join();
            System.out.printf("2. Same pass id issued by 2 counters at once: succeeded=%d, rejected by primary key=%d%n",
                    won.get(), lost.get());

            // 3. Search at scale: indexed lookup in the database vs scanning the legacy text file
            try (Statement st = admin.createStatement()) { st.execute("TRUNCATE TABLE bus_pass"); }
            Path legacy = Path.of(args.length > 0 ? args[0] : "legacy_passes.csv");
            if (Files.exists(legacy)) {
                long rows;
                try (PreparedStatement ps = admin.prepareStatement(INS)) {
                    admin.setAutoCommit(false);
                    int n = 0;
                    for (String line : Files.readAllLines(legacy)) {
                        String[] f = line.split(",", -1);
                        if (f.length != 5) continue;
                        try {
                            ps.setString(1, f[0]); ps.setString(2, f[1]); ps.setString(3, f[2]);
                            ps.setInt(4, Integer.parseInt(f[3])); ps.setDouble(5, Double.parseDouble(f[4]));
                        } catch (NumberFormatException e) { continue; }
                        ps.addBatch();
                        if (++n % 1000 == 0) { try { ps.executeBatch(); } catch (SQLException e) { /* duplicates ignored in this demo */ } }
                    }
                    try { ps.executeBatch(); } catch (SQLException e) { /* ignore */ }
                    admin.commit();
                    admin.setAutoCommit(true);
                    rows = count(admin);
                }
                int q = 1000;
                try (PreparedStatement ps = admin.prepareStatement("SELECT * FROM bus_pass WHERE pass_id=?")) {
                    for (int i = 0; i < 200; i++) { ps.setString(1, "P0050000"); try (ResultSet rs = ps.executeQuery()) { rs.next(); } }
                    t = System.nanoTime();
                    for (int i = 0; i < q; i++) {
                        ps.setString(1, String.format("P%07d", 1 + (i * 7919) % 99000));
                        try (ResultSet rs = ps.executeQuery()) { rs.next(); }
                    }
                }
                double dbMs = (System.nanoTime() - t) / 1e6 / q;
                // file scan for the same kind of search (average of 20 scans, ids spread over the file)
                int fq = 20;
                t = System.nanoTime();
                for (int i = 0; i < fq; i++) {
                    String want = String.format("P%07d", 1 + (i * 4999) % 99000);
                    try (java.io.BufferedReader r = Files.newBufferedReader(legacy)) {
                        String line;
                        while ((line = r.readLine()) != null) if (line.startsWith(want + ",")) break;
                    }
                }
                double fileMs = (System.nanoTime() - t) / 1e6 / fq;
                System.out.printf("3. Find one pass among %d rows: database primary-key lookup = %.3f ms, text-file scan = %.2f ms (%.0fx slower)%n",
                        rows, dbMs, fileMs, fileMs / dbMs);
                try (PreparedStatement ps = admin.prepareStatement("SELECT COUNT(*) FROM bus_pass WHERE route_no=?")) {
                    ps.setInt(1, 7);
                    t = System.nanoTime();
                    try (ResultSet rs = ps.executeQuery()) { rs.next(); System.out.printf("   passes on route 7 (indexed query): %d in %.2f ms%n", rs.getLong(1), (System.nanoTime() - t) / 1e6); }
                }
            } else {
                System.out.println("3. (skipped: legacy file not found)");
            }
            try (Statement st = admin.createStatement()) { st.execute("TRUNCATE TABLE bus_pass"); }
        }
    }
}
