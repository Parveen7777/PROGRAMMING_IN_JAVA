import java.util.*;
import java.util.function.Consumer;

/** Task 1 alternative analysis: if-else chain on command string  vs  one listener per command (HashMap -> O(1)). */
public class DispatchBenchmark {
    static long sink;
    public static void main(String[] args) {
        int[] ks = {3, 30, 300, 3000};
        int calls = 2_000_000;
        System.out.printf("%-8s %-22s %-22s%n", "k", "if-else chain (ns/op)", "listener map (ns/op)");
        for (int k : ks) {
            String[] cmds = new String[k];
            Map<String, Consumer<String>> map = new HashMap<>();
            for (int i = 0; i < k; i++) { cmds[i] = "Cmd" + i; map.put(cmds[i], s -> sink++); }
            String target = cmds[k - 1];                // worst case: last button
            for (int w = 0; w < 3; w++) { chain(cmds, target, 200_000); mapped(map, target, 200_000); }   // warm-up
            long t1 = System.nanoTime(); chain(cmds, target, calls); long a = System.nanoTime() - t1;
            long t2 = System.nanoTime(); mapped(map, target, calls); long b = System.nanoTime() - t2;
            System.out.printf("%-8d %-22.1f %-22.1f%n", k, a / (double) calls, b / (double) calls);
        }
    }
    static void chain(String[] cmds, String c, int n) {
        for (int i = 0; i < n; i++) for (String s : cmds) if (s.equals(c)) { sink++; break; }
    }
    static void mapped(Map<String, Consumer<String>> m, String c, int n) {
        for (int i = 0; i < n; i++) m.get(c).accept(c);
    }
}
