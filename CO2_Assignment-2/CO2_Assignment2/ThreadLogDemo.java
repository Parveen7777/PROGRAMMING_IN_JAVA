import java.util.*;
import java.util.concurrent.*;

/** Q1 Design decision - shared booking log appended by several threads. */
public class ThreadLogDemo {
    static final int THREADS = 8, APPENDS = 100_000, TRIALS = 5;
    static final String LINE = "LOG\n";

    interface Appender { void append(String s); int length(); }

    static int[] trial(Appender a) throws Exception {
        int[] errors = {0};
        Thread[] ts = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            ts[i] = new Thread(() -> {
                try { for (int k = 0; k < APPENDS; k++) a.append(LINE); }
                catch (Throwable t) { synchronized (errors) { errors[0]++; } }
            });
        }
        for (Thread t : ts) t.start();
        for (Thread t : ts) t.join();
        return new int[]{a.length(), errors[0]};
    }

    public static void main(String[] args) throws Exception {
        int expected = THREADS * APPENDS * LINE.length();
        System.out.println("Expected final length = " + expected + " chars (" + THREADS + " threads x " + APPENDS + " appends)");
        String[] names = {"StringBuilder (unsafe)", "StringBuffer", "StringBuilder + synchronized", "ConcurrentLinkedQueue<String>"};
        for (String name : names) {
            System.out.println("\n" + name);
            int bad = 0; long totalMs = 0;
            for (int t = 1; t <= TRIALS; t++) {
                Appender a;
                switch (name) {
                    case "StringBuilder (unsafe)": { StringBuilder sb = new StringBuilder(); a = new Appender() {
                        public void append(String s) { sb.append(s); } public int length() { return sb.length(); } }; break; }
                    case "StringBuffer": { StringBuffer sb = new StringBuffer(); a = new Appender() {
                        public void append(String s) { sb.append(s); } public int length() { return sb.length(); } }; break; }
                    case "StringBuilder + synchronized": { StringBuilder sb = new StringBuilder(); a = new Appender() {
                        public void append(String s) { synchronized (sb) { sb.append(s); } } public int length() { return sb.length(); } }; break; }
                    default: { ConcurrentLinkedQueue<String> q = new ConcurrentLinkedQueue<>(); a = new Appender() {
                        public void append(String s) { q.add(s); } public int length() { return q.size() * LINE.length(); } }; }
                }
                long t0 = System.nanoTime();
                int[] r = trial(a);
                long ms = (System.nanoTime() - t0) / 1_000_000; totalMs += ms;
                boolean ok = r[0] == expected && r[1] == 0; if (!ok) bad++;
                System.out.printf("  trial %d: length=%d (lost %d chars), thread exceptions=%d, %d ms -> %s%n",
                        t, r[0], expected - r[0], r[1], ms, ok ? "CORRECT" : "CORRUPTED");
            }
            System.out.printf("  => %d of %d trials corrupted, average %d ms%n", bad, TRIALS, totalMs / TRIALS);
        }
    }
}
