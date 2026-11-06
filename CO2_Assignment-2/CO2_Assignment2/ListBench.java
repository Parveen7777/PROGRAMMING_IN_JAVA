import java.io.*;
import java.util.*;
import java.util.function.Supplier;

/** Q2 - ArrayList vs LinkedList vs ArrayDeque (vs plain array) for a playlist editor. */
public class ListBench {
    static volatile long sink;
    static final int OPS = 1000, RUNS = 5;

    interface Task { void run(); }

    /** avg ms of RUNS runs; setup is excluded from the timing. */
    static double time(Supplier<Task> setup) {
        setup.get().run();                       // warm-up (JIT)
        double total = 0;
        for (int r = 0; r < RUNS; r++) {
            Task t = setup.get();
            long t0 = System.nanoTime(); t.run(); total += (System.nanoTime() - t0) / 1e6;
        }
        return total / RUNS;
    }

    static List<Integer> filled(Supplier<List<Integer>> mk, int n) {
        List<Integer> l = mk.get(); for (int i = 0; i < n; i++) l.add(i); return l;
    }

    // ---- plain array with manual shifting -------------------------------
    static class PlainArray {
        int[] a; int size;
        PlainArray(int cap) { a = new int[cap]; }
        void addEnd(int v) { a[size++] = v; }
        void insert(int idx, int v) { for (int i = size; i > idx; i--) a[i] = a[i - 1]; a[idx] = v; size++; }
        int get(int i) { return a[i]; }
    }

    public static void main(String[] args) throws Exception {
        int[] sizes = {10_000, 50_000, 200_000};
        String[] ops = {"addEnd(n adds)", "insertFront(x1000)", "insertMiddle(x1000)", "removeFront(x1000)",
                        "removeMiddle(x1000)", "getKth(x1000 random)", "iterate(all)"};
        PrintWriter csv = new PrintWriter("q2_results.csv");
        csv.println("n,operation,ArrayList_ms,LinkedList_ms,ArrayDeque_ms,PlainArray_ms");
        System.out.println("=== Q2: avg ms over " + RUNS + " runs (after warm-up). N/A = not supported by the structure ===");
        for (int n : sizes) {
            Random rnd = new Random(42);
            int[] idx = new int[OPS]; for (int i = 0; i < OPS; i++) idx[i] = rnd.nextInt(n);
            System.out.println("\nPlaylist size n = " + n);
            System.out.printf("%-22s %12s %12s %12s %12s%n", "Operation", "ArrayList", "LinkedList", "ArrayDeque", "PlainArray");
            Supplier<List<Integer>> AL = ArrayList::new, LL = LinkedList::new;
            for (String op : ops) {
                Double al, ll, ad = null, pa = null;
                switch (op) {
                    case "addEnd(n adds)":
                        al = time(() -> () -> { List<Integer> l = new ArrayList<>(); for (int i = 0; i < n; i++) l.add(i); sink += l.size(); });
                        ll = time(() -> () -> { List<Integer> l = new LinkedList<>(); for (int i = 0; i < n; i++) l.add(i); sink += l.size(); });
                        ad = time(() -> () -> { ArrayDeque<Integer> d = new ArrayDeque<>(); for (int i = 0; i < n; i++) d.addLast(i); sink += d.size(); });
                        pa = time(() -> () -> { PlainArray p = new PlainArray(n + 1); for (int i = 0; i < n; i++) p.addEnd(i); sink += p.size; });
                        break;
                    case "insertFront(x1000)":
                        al = time(() -> { List<Integer> l = filled(AL, n); return () -> { for (int i = 0; i < OPS; i++) l.add(0, i); sink += l.size(); }; });
                        ll = time(() -> { List<Integer> l = filled(LL, n); return () -> { for (int i = 0; i < OPS; i++) l.add(0, i); sink += l.size(); }; });
                        ad = time(() -> { ArrayDeque<Integer> d = new ArrayDeque<>(filled(AL, n)); return () -> { for (int i = 0; i < OPS; i++) d.addFirst(i); sink += d.size(); }; });
                        pa = time(() -> { PlainArray p = new PlainArray(n + OPS); for (int i = 0; i < n; i++) p.addEnd(i); return () -> { for (int i = 0; i < OPS; i++) p.insert(0, i); sink += p.size; }; });
                        break;
                    case "insertMiddle(x1000)":
                        al = time(() -> { List<Integer> l = filled(AL, n); return () -> { for (int i = 0; i < OPS; i++) l.add(l.size() / 2, i); sink += l.size(); }; });
                        ll = time(() -> { List<Integer> l = filled(LL, n); return () -> { for (int i = 0; i < OPS; i++) l.add(l.size() / 2, i); sink += l.size(); }; });
                        pa = time(() -> { PlainArray p = new PlainArray(n + OPS); for (int i = 0; i < n; i++) p.addEnd(i); return () -> { for (int i = 0; i < OPS; i++) p.insert(p.size / 2, i); sink += p.size; }; });
                        break;
                    case "removeFront(x1000)":
                        al = time(() -> { List<Integer> l = filled(AL, n); return () -> { for (int i = 0; i < OPS; i++) l.remove(0); sink += l.size(); }; });
                        ll = time(() -> { List<Integer> l = filled(LL, n); return () -> { for (int i = 0; i < OPS; i++) l.remove(0); sink += l.size(); }; });
                        ad = time(() -> { ArrayDeque<Integer> d = new ArrayDeque<>(filled(AL, n)); return () -> { for (int i = 0; i < OPS; i++) d.pollFirst(); sink += d.size(); }; });
                        break;
                    case "removeMiddle(x1000)":
                        al = time(() -> { List<Integer> l = filled(AL, n); return () -> { for (int i = 0; i < OPS; i++) l.remove(l.size() / 2); sink += l.size(); }; });
                        ll = time(() -> { List<Integer> l = filled(LL, n); return () -> { for (int i = 0; i < OPS; i++) l.remove(l.size() / 2); sink += l.size(); }; });
                        break;
                    case "getKth(x1000 random)":
                        al = time(() -> { List<Integer> l = filled(AL, n); return () -> { long s = 0; for (int i : idx) s += l.get(i); sink += s; }; });
                        ll = time(() -> { List<Integer> l = filled(LL, n); return () -> { long s = 0; for (int i : idx) s += l.get(i); sink += s; }; });
                        pa = time(() -> { PlainArray p = new PlainArray(n); for (int i = 0; i < n; i++) p.addEnd(i); return () -> { long s = 0; for (int i : idx) s += p.get(i); sink += s; }; });
                        break;
                    default: // iterate
                        al = time(() -> { List<Integer> l = filled(AL, n); return () -> { long s = 0; for (int v : l) s += v; sink += s; }; });
                        ll = time(() -> { List<Integer> l = filled(LL, n); return () -> { long s = 0; for (int v : l) s += v; sink += s; }; });
                        ad = time(() -> { ArrayDeque<Integer> d = new ArrayDeque<>(filled(AL, n)); return () -> { long s = 0; for (int v : d) s += v; sink += s; }; });
                        pa = time(() -> { PlainArray p = new PlainArray(n); for (int i = 0; i < n; i++) p.addEnd(i); return () -> { long s = 0; for (int i = 0; i < p.size; i++) s += p.a[i]; sink += s; }; });
                }
                System.out.printf("%-22s %12s %12s %12s %12s%n", op, f(al), f(ll), f(ad), f(pa));
                csv.printf("%d,%s,%s,%s,%s,%s%n", n, op, c(al), c(ll), c(ad), c(pa));
            }
        }
        csv.close();

        // ---- Changed condition: "Up Next" queue -----------------------------
        int preload = 50_000, total = 100_000;
        System.out.println("\n=== Changed condition: Up-Next queue, " + total + " operations (40% addLast, 20% addFirst, 40% removeFirst), starting size " + preload + " ===");
        Random rnd = new Random(7); int[] kind = new int[total];
        for (int i = 0; i < total; i++) { int r = rnd.nextInt(10); kind[i] = r < 4 ? 0 : r < 6 ? 1 : 2; }
        double al = time(() -> { List<Integer> l = filled(ArrayList::new, preload); return () -> { for (int i = 0; i < total; i++) { if (kind[i] == 0) l.add(i); else if (kind[i] == 1) l.add(0, i); else l.remove(0); } sink += l.size(); }; });
        double ll = time(() -> { LinkedList<Integer> l = (LinkedList<Integer>) filled(LinkedList::new, preload); return () -> { for (int i = 0; i < total; i++) { if (kind[i] == 0) l.addLast(i); else if (kind[i] == 1) l.addFirst(i); else l.removeFirst(); } sink += l.size(); }; });
        double ad = time(() -> { ArrayDeque<Integer> d = new ArrayDeque<>(filled(ArrayList::new, preload)); return () -> { for (int i = 0; i < total; i++) { if (kind[i] == 0) d.addLast(i); else if (kind[i] == 1) d.addFirst(i); else d.pollFirst(); } sink += d.size(); }; });
        System.out.printf("ArrayList  : %10.2f ms%nLinkedList : %10.2f ms%nArrayDeque : %10.2f ms%n", al, ll, ad);
        try (PrintWriter c2 = new PrintWriter("q2_upnext.csv")) {
            c2.println("structure,ms"); c2.printf("ArrayList,%.2f%nLinkedList,%.2f%nArrayDeque,%.2f%n", al, ll, ad);
        }
    }
    static String f(Double d) { return d == null ? "N/A" : String.format("%.3f", d); }
    static String c(Double d) { return d == null ? "" : String.format("%.3f", d); }
}
