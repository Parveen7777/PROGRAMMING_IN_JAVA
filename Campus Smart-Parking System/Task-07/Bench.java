import java.util.*;

public class Bench {
    static long sink;

    // ---------- (a) find by registration: parallel arrays vs object array
    static class Veh { String regNo; String type; long entry; Veh(String r, String t, long e){regNo=r;type=t;entry=e;} }

    static void benchSearch() {
        System.out.println("## A. Search by registration number (1000 worst-case searches)");
        for (int n : new int[]{1_000, 10_000, 100_000}) {
            String[] reg = new String[n], type = new String[n]; long[] entry = new long[n];
            Veh[] objs = new Veh[n];
            for (int i = 0; i < n; i++) { reg[i] = "TN" + i; type[i] = "CAR"; entry[i] = i; objs[i] = new Veh(reg[i], type[i], i); }
            String key = reg[n - 1];
            for (int w = 0; w < 3; w++) { sink += findPar(reg, key); sink += findObj(objs, key); }
            long t0 = System.nanoTime(); for (int k = 0; k < 1000; k++) sink += findPar(reg, key); long t1 = System.nanoTime();
            for (int k = 0; k < 1000; k++) sink += findObj(objs, key); long t2 = System.nanoTime();
            System.out.printf("A|%d|%.1f|%.1f%n", n, (t1 - t0) / 1000.0 / 1000, (t2 - t1) / 1000.0 / 1000);
        }
    }
    static int findPar(String[] reg, String k) { for (int i = 0; i < reg.length; i++) if (reg[i].equals(k)) return i; return -1; }
    static int findObj(Veh[] o, String k) { for (int i = 0; i < o.length; i++) if (o[i].regNo.equals(k)) return i; return -1; }

    // ---------- (b) allocation: scan vs free-stack
    static void benchAlloc() {
        System.out.println("## B. allocate()+release() on a nearly full facility");
        int[][] cfg = {{1, 20}, {3, 50}, {5, 100}, {50, 1000}};
        for (int[] c : cfg) {
            int F = c[0], S = c[1];
            String[][] slots = new String[F][S];
            for (String[] row : slots) Arrays.fill(row, "X");
            slots[F - 1][S - 1] = null;                      // only the LAST slot is free (worst case)
            int[] free = new int[]{F - 1, S - 1};
            ArrayDeque<int[]> stack = new ArrayDeque<>(); stack.push(free);
            int reps = 200_000 / Math.max(1, (F * S) / 1000 + 1);
            for (int w = 0; w < 30000 && w * (long) F * S < 300_000_000L; w++) { int[] p = scan(slots); slots[p[0]][p[1]] = null; }
            long t0 = System.nanoTime();
            for (int r = 0; r < reps; r++) { int[] p = scan(slots); slots[p[0]][p[1]] = null; }
            long t1 = System.nanoTime();
            for (int r = 0; r < reps; r++) { int[] p = stack.pop(); stack.push(p); }
            long t2 = System.nanoTime();
            System.out.printf("B|%d|%d|%.1f|%.1f%n", F, S, (t1 - t0) / (double) reps, (t2 - t1) / (double) reps);
        }
    }
    static int[] scan(String[][] s) {
        for (int f = 0; f < s.length; f++) for (int j = 0; j < s[f].length; j++)
            if (s[f][j] == null) { s[f][j] = "X"; return new int[]{f, j}; }
        return null;
    }

    // ---------- (c) ticket lookup: array vs ArrayList vs HashMap
    static void benchLookup() {
        System.out.println("## C. Find a ticket on exit (1000 random lookups)");
        for (int n : new int[]{1_000, 10_000, 100_000}) {
            String[] arr = new String[n]; List<String> list = new ArrayList<>(); Map<String, String> map = new HashMap<>();
            for (int i = 0; i < n; i++) { arr[i] = "TN" + i; list.add(arr[i]); map.put(arr[i], arr[i]); }
            Random rnd = new Random(7); String[] keys = new String[1000];
            for (int i = 0; i < 1000; i++) keys[i] = arr[rnd.nextInt(n)];
            for (int w = 0; w < 3; w++) for (String k : keys) { sink += findPar(arr, k); sink += list.indexOf(k); sink += map.get(k).length(); }
            long t0 = System.nanoTime(); for (String k : keys) sink += findPar(arr, k);
            long t1 = System.nanoTime(); for (String k : keys) sink += list.indexOf(k);
            long t2 = System.nanoTime(); for (String k : keys) sink += map.get(k).length();
            long t3 = System.nanoTime();
            System.out.printf("C|%d|%.1f|%.1f|%.3f%n", n, (t1 - t0) / 1000.0 / 1000, (t2 - t1) / 1000.0 / 1000, (t3 - t2) / 1000.0 / 1000);
        }
    }

    // ---------- (d) fee: dynamic dispatch vs instanceof chain vs switch on type code
    static abstract class V { abstract double rate(); double fee(int h){ return h * rate(); } }
    static class TW extends V { double rate(){return 10;} }
    static class CR extends V { double rate(){return 30;} }
    static class EV extends CR { }
    static class EB extends V { double rate(){return 20;} }
    static class VN extends V { double rate(){return 60;} }
    static double feeInstanceof(V v, int h) {
        if (v instanceof TW) return h * 10; else if (v instanceof EV) return h * 30;
        else if (v instanceof CR) return h * 30; else if (v instanceof EB) return h * 20; else return h * 60;
    }
    static double feeSwitch(int code, int h) {
        switch (code) { case 0: return h * 10; case 1: return h * 30; case 2: return h * 30; case 3: return h * 20; default: return h * 60; }
    }
    static void benchFee() {
        System.out.println("## D. Fee calculation, 20 million calls");
        V[] vs = {new TW(), new CR(), new EV(), new EB(), new VN()}; int[] codes = {0, 1, 2, 3, 4};
        int N = 20_000_000; double acc = 0;
        for (int w = 0; w < 3; w++) for (int i = 0; i < 2_000_000; i++) { acc += vs[i % 5].fee(3) + feeInstanceof(vs[i % 5], 3) + feeSwitch(codes[i % 5], 3); }
        long t0 = System.nanoTime(); for (int i = 0; i < N; i++) acc += vs[i % 5].fee(3);
        long t1 = System.nanoTime(); for (int i = 0; i < N; i++) acc += feeInstanceof(vs[i % 5], 3);
        long t2 = System.nanoTime(); for (int i = 0; i < N; i++) acc += feeSwitch(codes[i % 5], 3);
        long t3 = System.nanoTime(); sink += (long) acc;
        System.out.printf("D|%.2f|%.2f|%.2f%n", (t1 - t0) / (double) N, (t2 - t1) / (double) N, (t3 - t2) / (double) N);
    }

    // ---------- (e) memory per vehicle: parallel arrays vs objects (n = 1,000,000)
    static long used() { for (int i = 0; i < 4; i++) { System.gc(); } Runtime r = Runtime.getRuntime(); return r.totalMemory() - r.freeMemory(); }
    static void benchMem() {
        int n = 1_000_000;
        long b0 = used();
        String[] reg = new String[n]; String[] type = new String[n]; long[] entry = new long[n];
        String shared = "CAR";
        for (int i = 0; i < n; i++) { reg[i] = "TN" + i; type[i] = shared; entry[i] = i; }
        long b1 = used();
        Veh[] objs = new Veh[n];
        for (int i = 0; i < n; i++) objs[i] = new Veh("TN" + i, shared, i);
        long b2 = used();
        System.out.printf("E|%.1f|%.1f%n", (b1 - b0) / (double) n, (b2 - b1) / (double) n);
        sink += reg.length + type.length + entry.length + objs.length;
    }

    public static void main(String[] a) {
        System.out.println("JAVA|" + System.getProperty("java.version") + "|" + Runtime.getRuntime().availableProcessors());
        benchSearch(); benchAlloc(); benchAlloc(); benchAlloc(); benchLookup(); benchFee(); benchMem();
        System.out.println("sink " + sink);
    }
}
