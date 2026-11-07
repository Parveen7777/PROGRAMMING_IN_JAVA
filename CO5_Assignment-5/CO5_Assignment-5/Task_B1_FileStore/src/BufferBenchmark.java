import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/** B1.3 - time unbuffered vs buffered vs readAllLines on 1 MB, 10 MB and 50 MB logs. */
public class BufferBenchmark {

    static void generate(Path p, long targetBytes) throws IOException {
        long written = 0;
        int i = 0;
        try (BufferedWriter w = Files.newBufferedWriter(p)) {
            while (written < targetBytes) {
                String line = "2025-07-" + String.format("%02d", 1 + i % 28) + ",19231" +
                        String.format("%04d", i % 10000) + ",PRESENT";
                w.write(line);
                w.newLine();
                written += line.length() + 1;
                i++;
            }
        }
    }

    static long usedMemory() {
        System.gc();
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    public static void main(String[] args) throws Exception {
        long[] sizesMB = {1, 10, 50};
        Files.createDirectories(Path.of("results"));
        try (PrintWriter csv = new PrintWriter("results/buffering_results.csv")) {
            csv.println("sizeMB,lines,unbuffered_ms,buffered8k_ms,buffered64k_ms,readAllLines_ms,readAllLines_heap_MB");
            System.out.printf("%-8s %-10s %-14s %-14s %-14s %-16s %-10s%n",
                    "SizeMB", "Lines", "Unbuf(ms)", "Buf8K(ms)", "Buf64K(ms)", "ReadAllLines(ms)", "Heap(MB)");
            for (long mb : sizesMB) {
                Path p = Files.createTempFile("attendance", ".log");
                generate(p, mb * 1024 * 1024);
                LineCounter.countBuffered(p, 8192);   // warm-up so JIT does not distort the first row

                long t = System.nanoTime();
                long l1 = LineCounter.countUnbuffered(p);
                double unbuf = (System.nanoTime() - t) / 1e6;

                t = System.nanoTime();
                long l2 = LineCounter.countBuffered(p, 8192);
                double b8 = (System.nanoTime() - t) / 1e6;

                t = System.nanoTime();
                long l3 = LineCounter.countBuffered(p, 65536);
                double b64 = (System.nanoTime() - t) / 1e6;

                long before = usedMemory();
                t = System.nanoTime();
                java.util.List<String> all = Files.readAllLines(p);
                double ral = (System.nanoTime() - t) / 1e6;
                long after = usedMemory();
                double heapMB = (after - before) / (1024.0 * 1024.0);
                long l4 = all.size();
                all = null;

                if (l1 != l2 || l2 != l3 || l3 != l4) throw new IllegalStateException("counts differ!");
                System.out.printf("%-8d %-10d %-14.1f %-14.1f %-14.1f %-16.1f %-10.1f%n",
                        mb, l1, unbuf, b8, b64, ral, heapMB);
                csv.printf("%d,%d,%.1f,%.1f,%.1f,%.1f,%.1f%n", mb, l1, unbuf, b8, b64, ral, heapMB);
                System.out.printf("   predicted read() calls: unbuffered = %d (one per byte); buffered(8K) ~ %d%n",
                        Files.size(p), Files.size(p) / 8192 + 1);
                Files.delete(p);
            }
        }
    }
}
