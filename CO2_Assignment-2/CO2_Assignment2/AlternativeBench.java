import java.lang.management.ManagementFactory;
import java.util.*;
import java.io.*;

/** Q3 alternative concepts: HashSet<Student> vs HashMap<String,Student> vs TreeSet<Student> (comparator on regNo). */
public class AlternativeBench {
    @SuppressWarnings("deprecation")
    static final com.sun.management.ThreadMXBean TMX = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
    @SuppressWarnings("deprecation")
    static long alloc() { return TMX.getThreadAllocatedBytes(Thread.currentThread().getId()); }
    static long usedAfterGc() { System.gc(); System.gc(); Runtime r = Runtime.getRuntime(); return r.totalMemory() - r.freeMemory(); }
    static volatile long sink;

    static class Student {
        final String regNo, name;
        Student(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) { return o instanceof Student && regNo.equals(((Student) o).regNo); }
        @Override public int hashCode() { return regNo.hashCode(); }
    }

    public static void main(String[] args) throws Exception {
        PrintWriter csv = new PrintWriter("q3_alternatives.csv");
        csv.println("unique_students,structure,scans,avg_ms,ns_per_scan,bytes_alloc_per_unique,retained_bytes_per_unique,final_size");
        System.out.printf("%-10s %-26s %9s %10s %12s %14s %14s %9s%n", "unique n", "structure", "scans", "avg ms", "ns/scan", "alloc B/unique", "retained B/uniq", "size");
        for (int n : new int[]{10_000, 100_000, 500_000}) {
            // 2n scans: every student scanned twice (different objects), shuffled
            Student[] scans = new Student[2 * n];
            for (int i = 0; i < n; i++) { String r = String.valueOf(192000000 + i); scans[i] = new Student(r, "S" + i); scans[n + i] = new Student(r, "S" + i); }
            List<Student> l = Arrays.asList(scans); Collections.shuffle(l, new Random(1));
            int runs = n >= 500_000 ? 3 : 5;
            for (String s : new String[]{"HashSet<Student>", "HashMap<String,Student>", "TreeSet<Student>(cmp regNo)"}) {
                double total = 0, bytes = 0; int size = 0;
                for (int r = -1; r < runs; r++) { // r=-1 is warm-up
                    long a0 = alloc(), t0 = System.nanoTime();
                    size = fill(s, scans);
                    long t1 = System.nanoTime(), a1 = alloc();
                    if (r >= 0) { total += (t1 - t0) / 1e6; bytes += a1 - a0; }
                }
                // retained memory
                long before = usedAfterGc(); Object keep = build(s, scans); long after = usedAfterGc();
                sink += keep.hashCode() & 1;
                double ms = total / runs;
                System.out.printf("%-10d %-26s %9d %10.2f %12.1f %14.1f %14.1f %9d%n", n, s, scans.length, ms, ms * 1e6 / scans.length, bytes / runs / n, (after - before) / (double) n, size);
                csv.printf("%d,\"%s\",%d,%.3f,%.1f,%.1f,%.1f,%d%n", n, s, scans.length, ms, ms * 1e6 / scans.length, bytes / runs / n, (after - before) / (double) n, size);
                keep = null;
            }
        }
        csv.close();
    }
    static Object build(String s, Student[] scans) {
        switch (s) {
            case "HashSet<Student>": { Set<Student> x = new HashSet<>(); for (Student st : scans) x.add(st); return x; }
            case "HashMap<String,Student>": { Map<String, Student> x = new HashMap<>(); for (Student st : scans) x.putIfAbsent(st.regNo, st); return x; }
            default: { Set<Student> x = new TreeSet<>(Comparator.comparing((Student st) -> st.regNo)); for (Student st : scans) x.add(st); return x; }
        }
    }
    static int fill(String s, Student[] scans) {
        Object o = build(s, scans);
        return o instanceof Map ? ((Map<?, ?>) o).size() : ((Set<?>) o).size();
    }
}
