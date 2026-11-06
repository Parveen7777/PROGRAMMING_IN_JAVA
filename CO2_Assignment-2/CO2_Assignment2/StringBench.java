import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.function.Function;

/** Q1 - String (+), StringBuilder, StringBuffer (and alternatives) for e-ticket generation. */
public class StringBench {
    @SuppressWarnings("deprecation")
    static final com.sun.management.ThreadMXBean TMX =
            (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
    @SuppressWarnings("deprecation")
    static long alloc() { return TMX.getThreadAllocatedBytes(Thread.currentThread().getId()); }
    static volatile long sink;

    // ---- the approaches --------------------------------------------------
    static String plus(String[] f)  { String s = ""; for (String x : f) s = s + x; return s; }
    static String sb(String[] f)    { StringBuilder b = new StringBuilder(); for (String x : f) b.append(x); return b.toString(); }
    static String sbuf(String[] f)  { StringBuffer b = new StringBuffer(); for (String x : f) b.append(x); return b.toString(); }
    static String sbPre(String[] f) {
        int len = 0; for (String x : f) len += x.length();
        StringBuilder b = new StringBuilder(len); for (String x : f) b.append(x); return b.toString();
    }
    static String join(String[] f)   { return String.join("", f); }
    static String joiner(String[] f) { StringJoiner j = new StringJoiner(""); for (String x : f) j.add(x); return j.toString(); }

    static String[] fields(int n) {
        String[] a = new String[n];
        for (int i = 0; i < n; i++) a[i] = "F" + i + "=Value" + (i * 7 % 1000) + "; "; // ~16 chars
        return a;
    }

    /** returns {avgMsPerRun, sdMs, avgBytesAllocatedPerTicket} */
    static double[] measure(Function<String[], String> fn, String[] f, int tickets, int runs) {
        int warm = Math.min(tickets, 20000);
        for (int w = 0; w < 2; w++) for (int t = 0; t < warm; t++) sink += fn.apply(f).length();
        double[] ms = new double[runs]; double bytes = 0;
        for (int r = 0; r < runs; r++) {
            long a0 = alloc(), t0 = System.nanoTime();
            for (int t = 0; t < tickets; t++) sink += fn.apply(f).length();
            long t1 = System.nanoTime(), a1 = alloc();
            ms[r] = (t1 - t0) / 1e6; bytes += (a1 - a0) / (double) tickets;
        }
        double mean = Arrays.stream(ms).average().orElse(0);
        double sd = Math.sqrt(Arrays.stream(ms).map(v -> (v - mean) * (v - mean)).sum() / runs);
        return new double[]{mean, sd, bytes / runs};
    }

    static int resizes(int finalLen) { int cap = 16, c = 0; while (cap < finalLen) { cap = cap * 2 + 2; c++; } return c; }

    static long usedAfterGc() { System.gc(); Runtime r = Runtime.getRuntime(); return r.totalMemory() - r.freeMemory(); }

    public static void main(String[] args) throws Exception {
        Map<String, Function<String[], String>> approaches = new LinkedHashMap<>();
        approaches.put("String +", StringBench::plus);
        approaches.put("StringBuilder", StringBench::sb);
        approaches.put("StringBuffer", StringBench::sbuf);
        approaches.put("StringBuilder(presized)", StringBench::sbPre);
        approaches.put("String.join", StringBench::join);
        approaches.put("StringJoiner", StringBench::joiner);

        // {name, tickets per run, fields per ticket, runs}
        Object[][] workloads = {
            {"W1 short ticket (10 fields, 100000 tickets)", 100000, 10, 5},
            {"W2 normal batch (50 fields, 10000 tickets)", 10000, 50, 5},
            {"W3 group booking (1000 fields, 200 tickets)", 200, 1000, 5}
        };
        PrintWriter csv = new PrintWriter("q1_results.csv");
        csv.println("workload,approach,avg_ms_per_run,sd_ms,us_per_ticket,bytes_alloc_per_ticket,est_intermediate_objects_per_ticket");
        System.out.println("=== Q1: workload comparison (avg of 5 runs, after warm-up) ===");
        for (Object[] w : workloads) {
            String name = (String) w[0]; int tickets = (Integer) w[1], nf = (Integer) w[2], runs = (Integer) w[3];
            String[] f = fields(nf);
            int finalLen = plus(f).length();
            System.out.println("\n" + name + "  [final ticket length = " + finalLen + " chars]");
            System.out.printf("%-26s %12s %9s %14s %16s %14s%n", "Approach", "avg ms/run", "sd ms", "us/ticket", "bytes/ticket", "est. interm.");
            for (var e : approaches.entrySet()) {
                double[] m = measure(e.getValue(), f, tickets, runs);
                String est = e.getKey().equals("String +") ? String.valueOf(nf)
                        : e.getKey().equals("StringBuilder") || e.getKey().equals("StringBuffer") ? String.valueOf(resizes(finalLen)) : "0";
                System.out.printf("%-26s %12.2f %9.2f %14.2f %16.0f %14s%n", e.getKey(), m[0], m[1], m[0] * 1000 / tickets, m[2], est);
                csv.printf("\"%s\",%s,%.3f,%.3f,%.3f,%.0f,%s%n", name, e.getKey(), m[0], m[1], m[0] * 1000 / tickets, m[2], est);
            }
        }
        csv.close();

        // ---- scaling with number of fields (single ticket repeated) -------
        System.out.println("\n=== Scaling with fields per ticket (20 tickets/run, avg of 5 runs) ===");
        PrintWriter c2 = new PrintWriter("q1_scaling.csv");
        c2.println("fields,plus_us_per_ticket,sb_us_per_ticket,plus_bytes,sb_bytes");
        System.out.printf("%8s %18s %18s %14s %14s %8s%n", "fields", "plus us/ticket", "sb us/ticket", "plus bytes", "sb bytes", "ratio");
        for (int n : new int[]{100, 250, 500, 1000, 2000, 4000, 8000}) {
            String[] f = fields(n);
            double[] p = measure(StringBench::plus, f, 20, 5), s = measure(StringBench::sb, f, 20, 5);
            double pu = p[0] * 1000 / 20, su = s[0] * 1000 / 20;
            System.out.printf("%8d %18.1f %18.1f %14.0f %14.0f %8.1f%n", n, pu, su, p[2], s[2], pu / su);
            c2.printf("%d,%.2f,%.2f,%.0f,%.0f%n", n, pu, su, p[2], s[2]);
        }
        c2.close();

        // ---- retained vs transient memory (W2: 10,000 tickets kept in a list) ----
        System.out.println("\n=== Memory (W2: 10000 tickets x 50 fields, kept in a List) ===");
        String[] f = fields(50);
        for (String key : new String[]{"String +", "StringBuilder"}) {
            Function<String[], String> fn = approaches.get(key);
            long before = usedAfterGc(); long a0 = alloc();
            List<String> keep = new ArrayList<>();
            for (int t = 0; t < 10000; t++) keep.add(fn.apply(f));
            long a1 = alloc(); long after = usedAfterGc();
            long retained = after - before, allocated = a1 - a0;
            System.out.printf("%-14s heap before=%,d B  retained after GC=%,d B  total allocated=%,d B  garbage=%,d B (%.1fx retained)%n",
                    key, before, retained, allocated, allocated - retained, (allocated - retained) / (double) Math.max(1, retained));
            sink += keep.size();
        }
    }
}
