import java.io.*;
import java.util.*;

/** Q4 - HashMap vs TreeMap vs LinkedHashMap (vs Hashtable, vs sorted array + binarySearch) for pharmacy stock. */
public class MedicineBench {
    static volatile long sink;

    static class Medicine {
        final String code, name; int stock;
        Medicine(String c, String n, int s) { code = c; name = n; stock = s; }
    }

    static Medicine[] makeData(int n) {                       // codes inserted in RANDOM order (not code order)
        List<Integer> ids = new ArrayList<>(); for (int i = 0; i < n; i++) ids.add(i);
        Collections.shuffle(ids, new Random(11));
        Medicine[] m = new Medicine[n];
        for (int i = 0; i < n; i++) { int id = ids.get(i); m[i] = new Medicine(String.format("MED%07d", id), "Drug-" + id, id % 500); }
        return m;
    }

    // a report = visit every medicine in some order and fold its data into a checksum
    static long codeOrderReport(String type, Map<String, Medicine> map) {
        Collection<Medicine> vals;
        if (type.equals("TreeMap")) vals = map.values();                         // natural: already sorted
        else { List<String> keys = new ArrayList<>(map.keySet()); Collections.sort(keys);   // extra work: sort keys
               List<Medicine> l = new ArrayList<>(keys.size()); for (String k : keys) l.add(map.get(k)); vals = l; }
        long cs = 0; for (Medicine m : vals) cs = cs * 31 + m.code.hashCode() + m.stock; return cs;
    }
    static boolean sortedOk(String type, Map<String, Medicine> map) {
        String prev = ""; Iterator<Medicine> it;
        if (type.equals("TreeMap")) it = map.values().iterator();
        else { List<String> keys = new ArrayList<>(map.keySet()); Collections.sort(keys); List<Medicine> l = new ArrayList<>(); for (String k : keys) l.add(map.get(k)); it = l.iterator(); }
        while (it.hasNext()) { String c = it.next().code; if (c.compareTo(prev) <= 0) return false; prev = c; }
        return true;
    }
    static long insertionOrderReport(Map<String, Medicine> map, List<String> sideList) {
        long cs = 0;
        if (sideList == null) { for (Medicine m : map.values()) cs = cs * 31 + m.code.hashCode() + m.stock; }  // LinkedHashMap natural
        else { for (String k : sideList) { Medicine m = map.get(k); cs = cs * 31 + m.code.hashCode() + m.stock; } } // extra structure + n lookups
        return cs;
    }
    static boolean insertionOk(Map<String, Medicine> map, List<String> sideList, Medicine[] data) {
        Iterator<String> it = sideList == null ? map.keySet().iterator() : sideList.iterator();
        for (Medicine m : data) if (!it.hasNext() || !it.next().equals(m.code)) return false;
        return true;
    }

    static Map<String, Medicine> mk(String t) {
        switch (t) { case "HashMap": return new HashMap<>(); case "TreeMap": return new TreeMap<>();
                     case "LinkedHashMap": return new LinkedHashMap<>(); default: return new Hashtable<>(); }
    }

    public static void main(String[] args) throws Exception {
        String[] types = {"HashMap", "TreeMap", "LinkedHashMap", "Hashtable"};
        PrintWriter csv = new PrintWriter("q4_results.csv");
        csv.println("n,structure,put_ms,lookup100k_ms,ns_per_lookup,iterate_ms,code_report_ms,code_report_sorted_ok,insertion_report_ms,insertion_report_order_ok");
        System.out.println("avg of 5 runs (3 for n=500000) after warm-up. Times in ms.");
        System.out.printf("%-8s %-14s %9s %12s %10s %10s %14s %8s %16s %8s%n", "n", "structure", "put(n)", "100k lookups", "ns/lookup", "iterate", "code-order rpt", "sorted?", "insertion rpt", "order?");
        for (int n : new int[]{1_000, 10_000, 100_000, 500_000}) {
            Medicine[] data = makeData(n);
            Random rnd = new Random(5); String[] q = new String[100_000];
            for (int i = 0; i < q.length; i++) q[i] = data[rnd.nextInt(n)].code;
            int runs = n >= 500_000 ? 3 : 5;
            for (String t : types) {
                double put = 0, look = 0, iter = 0, rc = 0, ri = 0; boolean sOk = false, oOk = false;
                for (int r = -1; r < runs; r++) {
                    long t0 = System.nanoTime();
                    Map<String, Medicine> map = mk(t); List<String> side = null;
                    if (!t.equals("LinkedHashMap")) side = new ArrayList<>(n);
                    for (Medicine m : data) { map.put(m.code, m); if (side != null) side.add(m.code); }
                    double p = (System.nanoTime() - t0) / 1e6;
                    t0 = System.nanoTime(); long s = 0; for (String k : q) s += map.get(k).stock; double l = (System.nanoTime() - t0) / 1e6; sink += s;
                    t0 = System.nanoTime(); s = 0; for (Medicine m : map.values()) s += m.stock; double it = (System.nanoTime() - t0) / 1e6; sink += s;
                    t0 = System.nanoTime(); sink += codeOrderReport(t, map); double c = (System.nanoTime() - t0) / 1e6;
                    t0 = System.nanoTime(); sink += insertionOrderReport(map, side); double i2 = (System.nanoTime() - t0) / 1e6;
                    if (r >= 0) { put += p; look += l; iter += it; rc += c; ri += i2; }
                    if (r == runs - 1) { sOk = sortedOk(t, map); oOk = insertionOk(map, side, data); }
                }
                put /= runs; look /= runs; iter /= runs; rc /= runs; ri /= runs;
                System.out.printf("%-8d %-14s %9.2f %12.2f %10.1f %10.2f %14.2f %8s %16.2f %8s%n", n, t, put, look, look * 1e6 / 100_000, iter, rc, sOk, ri, oOk);
                csv.printf("%d,%s,%.3f,%.3f,%.1f,%.3f,%.3f,%s,%.3f,%s%n", n, t, put, look, look * 1e6 / 100_000, iter, rc, sOk, ri, oOk);
            }
            // sorted array + binarySearch alternative
            double build = 0, look = 0, ins = 0, rep = 0;
            for (int r = -1; r < runs; r++) {
                long t0 = System.nanoTime();
                Medicine[] arr = data.clone(); Arrays.sort(arr, Comparator.comparing(m -> m.code));
                String[] keys = new String[n]; for (int i = 0; i < n; i++) keys[i] = arr[i].code;
                double b = (System.nanoTime() - t0) / 1e6;
                t0 = System.nanoTime(); long s = 0; for (String k : q) s += arr[Arrays.binarySearch(keys, k)].stock; double l = (System.nanoTime() - t0) / 1e6; sink += s;
                // 1000 new medicines inserted keeping order (shift on each insert)
                String[] k2 = Arrays.copyOf(keys, n + 1000); Medicine[] a2 = Arrays.copyOf(arr, n + 1000); int size = n;
                Random rr = new Random(9);
                t0 = System.nanoTime();
                for (int i = 0; i < 1000; i++) {
                    String nc = String.format("MED%07dX", rr.nextInt(n)); int bs = Arrays.binarySearch(k2, 0, size, nc); int pos = bs >= 0 ? bs : -bs - 1; // random position inside the array
                    System.arraycopy(k2, pos, k2, pos + 1, size - pos); System.arraycopy(a2, pos, a2, pos + 1, size - pos);
                    k2[pos] = nc; a2[pos] = new Medicine(nc, "x", 1); size++;
                }
                double in = (System.nanoTime() - t0) / 1e6;
                t0 = System.nanoTime(); long cs = 0; for (int i = 0; i < n; i++) cs = cs * 31 + arr[i].code.hashCode() + arr[i].stock; double rp = (System.nanoTime() - t0) / 1e6; sink += cs;
                if (r >= 0) { build += b; look += l; ins += in; rep += rp; }
            }
            build /= runs; look /= runs; ins /= runs; rep /= runs;
            System.out.printf("%-8d %-14s build(sort)=%.2f ms  100k lookups=%.2f ms (%.1f ns each)  1000 inserts=%.2f ms (%.2f us each)  code-order report=%.2f ms%n",
                    n, "SortedArray+BS", build, look, look * 1e6 / 100_000, ins, ins * 1000 / 1000, rep);
            csv.printf("%d,SortedArray+BS,%.3f,%.3f,%.1f,%.3f,%.3f,true,,%n", n, build, look, look * 1e6 / 100_000, rep, rep);
            csv.printf("%d,SortedArray_1000_inserts_ms,%.3f,,,,,,,%n", n, ins);
        }
        csv.close();
    }
}
