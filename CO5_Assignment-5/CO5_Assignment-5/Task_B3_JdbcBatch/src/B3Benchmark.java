import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/** B3.2 - time 1,000 / 10,000 / 100,000 inserts for every strategy. */
public class B3Benchmark {
    interface Loader { int load(Connection c, List<String[]> p) throws Exception; }

    static long count(Connection con) throws Exception {
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM bus_pass")) {
            rs.next();
            return rs.getLong(1);
        }
    }

    public static void main(String[] args) throws Exception {
        int[] sizes = {1000, 10000, 100000};
        String[] names = {"a) Statement (auto-commit)", "b) PreparedStatement (auto-commit)",
                "c) Prepared + addBatch (auto-commit)", "d) Prepared batch + 1 transaction (PassLoader)",
                "e) multi-row INSERT (1 txn)", "f) LOAD DATA LOCAL INFILE"};
        Loader[] loaders = {LoaderVariants::statementAutoCommit, LoaderVariants::preparedAutoCommit,
                LoaderVariants::batchAutoCommit, PassLoader::loadAll,
                LoaderVariants::multiRowInsert, LoaderVariants::loadData};
        Files.createDirectories(Path.of("results"));
        String tag = System.getenv().getOrDefault("RUN_TAG", "default");
        System.out.println("JDBC URL: " + DbConfig.url());
        try (Connection con = DbConfig.connect();
             PrintWriter csv = new PrintWriter("results/insert_times_" + tag + ".csv")) {
            csv.println("strategy,n,time_ms,rows_per_second");
            System.out.printf("%-48s %-10s %-12s %-14s%n", "Strategy", "Rows", "Time(ms)", "Rows/sec");
            for (int n : sizes) {
                List<String[]> data = PassGenerator.generate(n);
                for (int i = 0; i < loaders.length; i++) {
                    try (Statement st = con.createStatement()) { st.execute("TRUNCATE TABLE bus_pass"); }
                    long t = System.nanoTime();
                    String note = "";
                    double ms;
                    try {
                        int loaded = loaders[i].load(con, data);
                        ms = (System.nanoTime() - t) / 1e6;
                        if (loaded != n || count(con) != n) note = "  <-- COUNT MISMATCH";
                    } catch (Exception e) {
                        System.out.printf("%-48s %-10d NOT AVAILABLE (%s)%n", names[i], n,
                                e.getMessage().split("\n")[0]);
                        continue;
                    }
                    System.out.printf("%-48s %-10d %-12.0f %-14.0f%s%n", names[i], n, ms, n / (ms / 1000.0), note);
                    csv.printf("\"%s\",%d,%.0f,%.0f%n", names[i], n, ms, n / (ms / 1000.0));
                }
                System.out.println();
            }
            try (Statement st = con.createStatement()) { st.execute("TRUNCATE TABLE bus_pass"); }
        }
    }
}
