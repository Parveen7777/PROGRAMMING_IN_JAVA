import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** B1.4 - time to find a pass as the file grows; plus the HashMap alternative (O(q*n) vs O(n+q)). */
public class GrowthBenchmark {

    static void generate(Path p, int n) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(p)) {
            for (int i = 1; i <= n; i++) {
                w.write(String.join(",", String.format("P%07d", i), "19231" + String.format("%04d", i % 10000),
                        "Student" + i, String.valueOf(1 + i % 30), String.valueOf(3000 + (i % 20) * 100.0)));
                w.newLine();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int[] sizes = {1000, 5000, 10000, 25000, 50000, 100000};
        int q = 100;      // number of searches for the scan-vs-map comparison
        Files.createDirectories(Path.of("results"));
        try (PrintWriter csv = new PrintWriter("results/growth_results.csv")) {
            csv.println("n,find_last_ms,find_missing_ms,q,scan_q_searches_ms,map_load_ms,map_q_lookups_ms,map_total_ms,map_heap_MB");
            System.out.printf("%-8s %-14s %-16s %-18s %-12s %-14s %-10s%n",
                    "n", "find last(ms)", "find missing(ms)", "q scans total(ms)", "map load(ms)", "map total(ms)", "heap(MB)");
            for (int n : sizes) {
                Path p = Files.createTempFile("growth", ".csv");
                generate(p, n);
                PassStore s = new PassStore(p);
                for (int w = 0; w < 40; w++) s.find(String.format("P%07d", n));   // warm-up

                int reps = 40;
                long t = System.nanoTime();
                for (int r = 0; r < reps; r++) s.find(String.format("P%07d", n));
                double last = (System.nanoTime() - t) / 1e6 / reps;

                t = System.nanoTime();
                for (int r = 0; r < reps; r++) s.find("P9999999");
                double missing = (System.nanoTime() - t) / 1e6 / reps;

                // q searches by repeated scanning: O(q*n)
                t = System.nanoTime();
                for (int i = 0; i < q; i++) s.find(String.format("P%07d", 1 + (i * 7919) % n));
                double scanQ = (System.nanoTime() - t) / 1e6;

                // HashMap: load once O(n), then q lookups O(q)
                System.gc();
                long before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                t = System.nanoTime();
                Map<String, String[]> map = new HashMap<>();
                for (String line : Files.readAllLines(p)) {
                    String[] f = PassStore.parse(line);
                    if (f != null) map.put(f[0], f);
                }
                double load = (System.nanoTime() - t) / 1e6;
                long after = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                t = System.nanoTime();
                int hits = 0;
                for (int i = 0; i < q; i++) if (map.get(String.format("P%07d", 1 + (i * 7919) % n)) != null) hits++;
                double lookups = (System.nanoTime() - t) / 1e6;
                if (hits != q) throw new IllegalStateException("map lookup mismatch");
                double heap = Math.max(0, (after - before) / (1024.0 * 1024.0));

                System.out.printf("%-8d %-14.2f %-16.2f %-18.1f %-12.1f %-14.1f %-10.1f%n",
                        n, last, missing, scanQ, load, load + lookups, heap);
                csv.printf("%d,%.2f,%.2f,%d,%.1f,%.1f,%.3f,%.1f,%.1f%n",
                        n, last, missing, q, scanQ, load, lookups, load + lookups, heap);
                Files.delete(p);
            }
        }
    }
}
