import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.*;

/**
 * Q7 - Diagnose, Redesign, Validate.
 * Chosen solution: Q4 pharmacy inventory.  Bottleneck found in Q4: HashMap gives O(1) lookup but the evening
 * "code-order report" must collect + SORT all n keys (O(n log n)) EVERY evening; TreeMap avoids the sort but makes
 * every lookup O(log n) string comparisons (about 10-20x slower than HashMap in the Q4 measurements).
 * Redesign: DualIndexInventory = LinkedHashMap (O(1) lookup + free insertion order) + a sorted Medicine[] that is
 * maintained INCREMENTALLY: only the k new medicines are sorted and then MERGED into the existing sorted array.
 */
public class PharmacyRedesign {
    @SuppressWarnings("deprecation")
    static final com.sun.management.ThreadMXBean TMX = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
    @SuppressWarnings("deprecation")
    static long alloc() { return TMX.getThreadAllocatedBytes(Thread.currentThread().getId()); }
    static long usedAfterGc() { System.gc(); System.gc(); Runtime r = Runtime.getRuntime(); return r.totalMemory() - r.freeMemory(); }
    static volatile long sink;

    static class Medicine {
        final String code, name; int stock; boolean active = true;
        Medicine(String c, String n, int s) { code = c; name = n; stock = s; }
    }
    static final Comparator<Medicine> BY_CODE = Comparator.comparing(m -> m.code);

    interface Inventory {
        void add(Medicine m); void remove(String code); Medicine find(String code);
        long codeOrderReport(); long insertionReport(); String label();
    }

    // ------------------------------------------------ ORIGINAL: HashMap + insertion list, sort on every report
    static class Original implements Inventory {
        final HashMap<String, Medicine> map = new HashMap<>(); final ArrayList<String> order = new ArrayList<>();
        public void add(Medicine m) { map.put(m.code, m); order.add(m.code); }
        public void remove(String c) { map.remove(c); order.remove(c); }
        public Medicine find(String c) { return map.get(c); }
        public long codeOrderReport() {
            List<String> keys = new ArrayList<>(map.keySet()); Collections.sort(keys);          // O(n log n) every evening
            long cs = 0; for (String k : keys) { Medicine m = map.get(k); cs = cs * 31 + m.code.hashCode() + m.stock; } return cs;
        }
        public long insertionReport() { long cs = 0; for (String k : order) { Medicine m = map.get(k); cs = cs * 31 + m.code.hashCode() + m.stock; } return cs; }
        public String label() { return "Original: HashMap + sort per report"; }
    }
    // ------------------------------------------------ BASELINE: TreeMap + insertion list
    static class TreeBased implements Inventory {
        final TreeMap<String, Medicine> map = new TreeMap<>(); final ArrayList<Medicine> order = new ArrayList<>();
        public void add(Medicine m) { map.put(m.code, m); order.add(m); }
        public void remove(String c) { Medicine m = map.remove(c); if (m != null) order.remove(m); }
        public Medicine find(String c) { return map.get(c); }
        public long codeOrderReport() { long cs = 0; for (Medicine m : map.values()) cs = cs * 31 + m.code.hashCode() + m.stock; return cs; }
        public long insertionReport() { long cs = 0; for (Medicine m : order) cs = cs * 31 + m.code.hashCode() + m.stock; return cs; }
        public String label() { return "Baseline: TreeMap + list"; }
    }
    // ------------------------------------------------ REDESIGN: LinkedHashMap + incrementally merged sorted array
    static class DualIndex implements Inventory {
        final LinkedHashMap<String, Medicine> byCode = new LinkedHashMap<>();   // O(1) lookup, insertion order for free
        Medicine[] sorted = new Medicine[0]; int sortedSize = 0;                // always code-ordered (after refresh)
        final ArrayList<Medicine> pending = new ArrayList<>(); int removed = 0; // changes since last report
        public void add(Medicine m) { byCode.put(m.code, m); pending.add(m); }
        public void remove(String c) { Medicine m = byCode.remove(c); if (m != null) { m.active = false; removed++; } }  // lazy deletion
        public Medicine find(String c) { return byCode.get(c); }
        int mergeCount = 0; long compares = 0;
        private int cmp(Medicine a, Medicine b) { compares++; return a.code.compareTo(b.code); }
        private void refresh() {
            if (pending.isEmpty() && removed == 0) return;                      // nothing changed -> reuse cached order, O(1)
            if (removed > 0) {                                                  // lazy deletions: one O(n) compaction pass, no string compares
                int w = 0; for (int i = 0; i < sortedSize; i++) if (sorted[i].active) sorted[w++] = sorted[i];
                Arrays.fill(sorted, w, sortedSize, null); sortedSize = w;
                pending.removeIf(m -> !m.active); removed = 0;
            }
            pending.sort(this::cmp);                                            // O(k log k) - only the NEW items are sorted
            Medicine[] out = new Medicine[sortedSize + pending.size()]; int prev = 0, o = 0;
            for (Medicine m : pending) {                                        // O(k log n) binary searches + bulk copies (memmove speed)
                int lo = prev, hi = sortedSize;
                while (lo < hi) { int mid = (lo + hi) >>> 1; if (cmp(sorted[mid], m) < 0) lo = mid + 1; else hi = mid; }
                System.arraycopy(sorted, prev, out, o, lo - prev); o += lo - prev; out[o++] = m; prev = lo;
            }
            System.arraycopy(sorted, prev, out, o, sortedSize - prev); o += sortedSize - prev;
            sorted = out; sortedSize = o; pending.clear(); mergeCount++;
        }
        public long codeOrderReport() { refresh(); long cs = 0; for (int i = 0; i < sortedSize; i++) { Medicine m = sorted[i]; cs = cs * 31 + m.code.hashCode() + m.stock; } return cs; }
        public long insertionReport() { long cs = 0; for (Medicine m : byCode.values()) cs = cs * 31 + m.code.hashCode() + m.stock; return cs; }
        public String label() { return "Redesign: LinkedHashMap + merged sorted array"; }
    }

    static Inventory make(int which) { return which == 0 ? new Original() : which == 1 ? new TreeBased() : new DualIndex(); }

    public static void main(String[] args) throws Exception {
        final int DAYS = 10, UPDATES = 1000, RUNS = 3;
        PrintWriter csv = new PrintWriter("q7_results.csv");
        csv.println("n0,new_per_day,structure,first_report_ms,avg_daily_code_report_ms,avg_daily_insertion_report_ms,alloc_MB_per_daily_code_report,lookups100k_ms,retained_MB,checksums_match");
        System.out.println("Setup: n0 medicines at start, then " + DAYS + " days; each day: +1% new medicines, " + UPDATES + " stock updates, one code-order report, one insertion-order report. Avg of " + RUNS + " runs.");
        System.out.printf("%-9s %-48s %10s %13s %13s %12s %12s %10s %9s%n", "n0", "structure", "1st rpt ms", "daily code ms", "daily ins ms", "MB/daily rpt", "100k lkp ms", "retained", "match?");
        for (int n0 : new int[]{50_000, 200_000, 500_000}) {
            int k = n0 / 100, total = n0 + DAYS * k;
            List<Integer> ids = new ArrayList<>(); for (int i = 0; i < total; i++) ids.add(i); Collections.shuffle(ids, new Random(21));
            Medicine[][] master = new Medicine[1][];       // fresh Medicine objects for each run (state is mutated)
            long[] refCode = new long[DAYS + 1], refIns = new long[DAYS + 1];
            for (int which = 0; which < 3; which++) {
                double first = 0, dcode = 0, dins = 0, mb = 0, look = 0; boolean match = true; double retained = 0;
                for (int run = -1; run < RUNS; run++) {
                    Medicine[] data = new Medicine[total];
                    for (int i = 0; i < total; i++) { int id = ids.get(i); data[i] = new Medicine(String.format("MED%07d", id), "Drug-" + id, id % 500); }
                    Inventory inv = make(which); for (int i = 0; i < n0; i++) inv.add(data[i]);
                    Random ur = new Random(99); double fR = 0, cR = 0, iR = 0; long aR = 0; int next = n0;
                    for (int day = 0; day <= DAYS; day++) {
                        if (day > 0) {
                            for (int i = 0; i < k; i++) inv.add(data[next++]);
                            for (int i = 0; i < UPDATES; i++) { Medicine m = inv.find(data[ur.nextInt(next)].code); m.stock = (m.stock + 7) % 500; }
                        }
                        long a0 = alloc(), t0 = System.nanoTime(); long cs = inv.codeOrderReport(); double ms = (System.nanoTime() - t0) / 1e6; long a1 = alloc();
                        t0 = System.nanoTime(); long ci = inv.insertionReport(); double ms2 = (System.nanoTime() - t0) / 1e6;
                        if (day == 0) fR = ms; else { cR += ms; iR += ms2; aR += a1 - a0; }
                        if (which == 0 && run == RUNS - 1) { refCode[day] = cs; refIns[day] = ci; }
                        if (run == RUNS - 1 && (cs != refCode[day] || ci != refIns[day])) match = false;
                    }
                    Random qr = new Random(5); String[] q = new String[100_000]; for (int i = 0; i < q.length; i++) q[i] = data[qr.nextInt(n0)].code;
                    long t0 = System.nanoTime(); long s = 0; for (String c : q) s += inv.find(c).stock; double lk = (System.nanoTime() - t0) / 1e6; sink += s;
                    if (run >= 0) { first += fR; dcode += cR / DAYS; dins += iR / DAYS; mb += aR / (double) DAYS / 1e6; look += lk; }
                    if (run == RUNS - 1) {                         // retained memory of the final structure
                        long before = usedAfterGc(); Inventory fresh = make(which); Medicine[] d2 = new Medicine[total];
                        for (int i = 0; i < total; i++) { int id = ids.get(i); d2[i] = new Medicine(String.format("MED%07d", id), "Drug-" + id, id % 500); }
                        long baseline = usedAfterGc();             // data only
                        for (int i = 0; i < total; i++) fresh.add(d2[i]); fresh.codeOrderReport();
                        long after = usedAfterGc(); retained = (after - baseline) / 1e6; sink += d2.length + fresh.find(d2[0].code).stock;
                    }
                }
                first /= RUNS; dcode /= RUNS; dins /= RUNS; mb /= RUNS; look /= RUNS;
                Inventory lab = make(which);
                System.out.printf("%-9d %-48s %10.1f %13.2f %13.2f %12.1f %12.2f %9.1fMB %9s%n", n0, lab.label(), first, dcode, dins, mb, look, retained, match);
                csv.printf("%d,%d,\"%s\",%.2f,%.3f,%.3f,%.2f,%.2f,%.1f,%s%n", n0, k, lab.label(), first, dcode, dins, mb, look, retained, match);
            }
            System.out.println();
        }
        csv.close();

        // ---- correctness of deletions in the redesign (lazy deletion + merge) ----
        System.out.println("Correctness check with deletions (50,000 medicines, remove 500, add 500, compare with original):");
        Original o = new Original(); DualIndex d = new DualIndex(); Random r = new Random(8);
        for (int i = 0; i < 50_000; i++) { String c = String.format("MED%07d", r.nextInt(10_000_000)); if (o.map.containsKey(c)) continue; o.add(new Medicine(c, "x", i % 500)); d.add(new Medicine(c, "x", i % 500)); }
        d.codeOrderReport();
        List<String> codes = new ArrayList<>(o.map.keySet()); Collections.shuffle(codes, r);
        for (int i = 0; i < 500; i++) { o.remove(codes.get(i)); d.remove(codes.get(i)); }
        for (int i = 0; i < 500; i++) { String c = String.format("NEW%07d", i * 37); o.add(new Medicine(c, "n", 5)); d.add(new Medicine(c, "n", 5)); }
        boolean ok = o.codeOrderReport() == d.codeOrderReport() && o.map.size() == d.byCode.size() && d.sortedSize == o.map.size();
        System.out.println("  code-order reports identical, sizes equal (" + o.map.size() + "): " + ok);
        int before = d.mergeCount; boolean same = d.codeOrderReport() == o.codeOrderReport();
        System.out.println("  second report with no changes reuses the cached order (merge count " + before + " -> " + d.mergeCount + "): " + (same && d.mergeCount == before));

        // ---- operation counts (string comparisons) for one daily report at n0 = 500,000, k = 5,000 ----
        System.out.println("\nOperation count: string comparisons needed for ONE evening code-order report (n0=500,000, k=5,000 new):");
        int n0 = 500_000, kk = 5_000; Random rr = new Random(1);
        List<String> all = new ArrayList<>(); Set<String> seen = new HashSet<>();
        while (all.size() < n0 + kk) { String c = String.format("MED%07d", rr.nextInt(100_000_000)); if (seen.add(c)) all.add(c); }
        long[] cnt = {0}; List<String> keys = new ArrayList<>(all.subList(0, n0 + kk)); Collections.shuffle(keys, rr);
        keys.sort((a, b) -> { cnt[0]++; return a.compareTo(b); });
        System.out.printf("  Original (re-sort all %d keys)        : %,d comparisons   (n log2 n = %,.0f)%n", n0 + kk, cnt[0], (n0 + kk) * (Math.log(n0 + kk) / Math.log(2)));
        DualIndex di = new DualIndex(); for (int i = 0; i < n0; i++) di.add(new Medicine(all.get(i), "x", 1));
        di.codeOrderReport(); long afterFirst = di.compares;
        for (int i = 0; i < kk; i++) di.add(new Medicine(all.get(n0 + i), "x", 1));
        di.codeOrderReport();
        System.out.printf("  Redesign (sort %d new + binary-search merge): %,d comparisons   (k log2 k + k log2 n = %,.0f)%n", kk, di.compares - afterFirst, kk * (Math.log(kk) / Math.log(2)) + kk * (Math.log(n0) / Math.log(2)));
    }
}
