import java.util.*;

/** Q6 - student-designed triage queue: correctness check, growth experiment, alternatives, starvation & fair rule. */
public class TriageDesign {
    static volatile long sink;

    static class Patient {
        final String name; final int severity; final long arrival; final long agedKey;
        Patient(String n, int s, long a, long step) { name = n; severity = s; arrival = a; agedKey = a + (s - 1) * step; }
        @Override public String toString() { return name + "(S" + severity + ")"; }
    }
    static final Comparator<Patient> RULE = Comparator.comparingInt((Patient p) -> p.severity).thenComparingLong(p -> p.arrival);
    static final Comparator<Patient> AGED = Comparator.comparingLong((Patient p) -> p.agedKey).thenComparingLong(p -> p.arrival);

    /** the design: PriorityQueue + monotonically increasing arrival counter as tie-breaker */
    static class TriageQueue {
        private final PriorityQueue<Patient> pq; private long counter;
        TriageQueue(Comparator<Patient> c) { pq = new PriorityQueue<>(c); }
        void admit(String name, int sev) { pq.add(new Patient(name, sev, counter++, STEP)); }
        Patient next() { return pq.poll(); }
        List<Patient> waitingInOrder() { List<Patient> l = new ArrayList<>(pq); l.sort(pq.comparator()); return l; } // O(n log n) copy, queue untouched
        PriorityQueue<Patient> raw() { return pq; }
    }
    static long STEP = 60;

    // ---- alternative 1: sorted LinkedList (insert at the right position)
    static class SortedLinked {
        final LinkedList<Patient> l = new LinkedList<>();
        void add(Patient p) { ListIterator<Patient> it = l.listIterator(); while (it.hasNext()) if (it.next().severity > p.severity) { it.previous(); break; } it.add(p); }
        Patient poll() { return l.pollFirst(); }
    }
    // ---- alternative 2: five FIFO queues
    static class FiveQueues {
        @SuppressWarnings("unchecked") final ArrayDeque<Patient>[] q = new ArrayDeque[5];
        FiveQueues() { for (int i = 0; i < 5; i++) q[i] = new ArrayDeque<>(); }
        void add(Patient p) { q[p.severity - 1].addLast(p); }
        Patient poll() { for (int i = 0; i < 5; i++) if (!q[i].isEmpty()) return q[i].pollFirst(); return null; }
    }

    public static void main(String[] args) throws Exception {
        // ======== Correctness check: 8 patients, order worked out by hand ========
        System.out.println("==== CORRECTNESS CHECK (8 patients) ====");
        String[] nm = {"Ravi", "Meena", "John", "Asha", "Kumar", "Lata", "Divya", "Imran"};
        int[] sv   = { 3,      1,       3,      5,      1,       2,      2,       4 };
        System.out.println("Arrival order: Ravi S3, Meena S1, John S3, Asha S5, Kumar S1, Lata S2, Divya S2, Imran S4");
        System.out.println("Hand-worked treatment order: Meena, Kumar (S1 by arrival); Lata, Divya (S2); Ravi, John (S3); Imran (S4); Asha (S5)");
        TriageQueue t = new TriageQueue(RULE);
        for (int i = 0; i < nm.length; i++) t.admit(nm[i], sv[i]);
        System.out.println("System.out.println(priorityQueue)  -> " + t.raw() + "   <-- heap ARRAY order, NOT treatment order");
        System.out.println("waitingInOrder() (sorted copy)     -> " + t.waitingInOrder());
        StringBuilder sb = new StringBuilder(); Patient p; while ((p = t.next()) != null) sb.append(p.name).append(' ');
        String got = sb.toString().trim();
        System.out.println("Served by poll()                   -> " + got);
        System.out.println("Matches hand-worked order: " + got.equals("Meena Kumar Lata Divya Ravi John Imran Asha"));

        // ======== Growth experiment ========
        System.out.println("\n==== GROWTH EXPERIMENT: avg of 5 runs (3 for 1M), ns per operation; add n then poll n ====");
        System.out.printf("%-10s %-22s %12s %12s %16s %16s%n", "n", "structure", "ns/add", "ns/poll", "compares/add", "compares/poll");
        java.io.PrintWriter csv = new java.io.PrintWriter("q6_growth.csv");
        csv.println("n,structure,ns_per_add,ns_per_poll,compares_per_add,compares_per_poll");
        for (int n : new int[]{1_000, 10_000, 30_000, 100_000, 1_000_000}) {
            Random r = new Random(4); Patient[] ps = new Patient[n];
            for (int i = 0; i < n; i++) ps[i] = new Patient("P" + i, 1 + r.nextInt(5), i, STEP);
            int runs = n >= 1_000_000 ? 3 : 5;
            for (String s : new String[]{"PriorityQueue", "SortedLinkedList", "FiveFifoQueues"}) {
                if (s.equals("SortedLinkedList") && n > 30_000) { System.out.printf("%-10d %-22s %12s %12s%n", n, s, "skipped", "(O(n^2) total - too slow)"); continue; }
                double add = 0, poll = 0;
                for (int run = -1; run < runs; run++) {
                    long t0, t1, t2;
                    if (s.equals("PriorityQueue")) { PriorityQueue<Patient> q = new PriorityQueue<>(RULE);
                        t0 = System.nanoTime(); for (Patient x : ps) q.add(x); t1 = System.nanoTime(); while ((p = q.poll()) != null) sink += p.severity; t2 = System.nanoTime(); }
                    else if (s.equals("SortedLinkedList")) { SortedLinked q = new SortedLinked();
                        t0 = System.nanoTime(); for (Patient x : ps) q.add(x); t1 = System.nanoTime(); while ((p = q.poll()) != null) sink += p.severity; t2 = System.nanoTime(); }
                    else { FiveQueues q = new FiveQueues();
                        t0 = System.nanoTime(); for (Patient x : ps) q.add(x); t1 = System.nanoTime(); while ((p = q.poll()) != null) sink += p.severity; t2 = System.nanoTime(); }
                    if (run >= 0) { add += (t1 - t0); poll += (t2 - t1); }
                }
                add = add / runs / n; poll = poll / runs / n;
                String ca = "", cp = "";
                if (s.equals("PriorityQueue")) {          // operation count: comparator calls
                    long[] cnt = {0}; Comparator<Patient> cc = (a, b) -> { cnt[0]++; return RULE.compare(a, b); };
                    PriorityQueue<Patient> q = new PriorityQueue<>(cc); for (Patient x : ps) q.add(x); long ad = cnt[0]; cnt[0] = 0; while (q.poll() != null) ; long po = cnt[0];
                    ca = String.format("%.2f", ad / (double) n); cp = String.format("%.2f", po / (double) n);
                }
                System.out.printf("%-10d %-22s %12.1f %12.1f %16s %16s%n", n, s, add, poll, ca, cp);
                csv.printf("%d,%s,%.1f,%.1f,%s,%s%n", n, s, add, poll, ca, cp);
            }
        }
        csv.close();

        // ======== Starvation and fair rule ========
        System.out.println("\n==== STARVATION: one severity-5 patient 'Old' arrives first; then every tick 1 new S1/S2 patient arrives and the doctor sees 1 patient ====");
        for (long step : new long[]{0, 30, 60}) {
            STEP = step; String label = step == 0 ? "pure priority (no aging)" : "aging, STEP=" + step + " arrivals per severity level";
            long worst = 0, best = Long.MAX_VALUE; int starved = 0;
            for (int seed = 0; seed < 20; seed++) {
                Random r = new Random(seed); TriageQueue q = new TriageQueue(step == 0 ? RULE : AGED);
                q.admit("Old", 5); long served = -1;
                for (long tick = 0; tick < 100_000; tick++) { q.admit("N" + tick, 1 + r.nextInt(2)); Patient s = q.next(); if (s.name.equals("Old")) { served = tick; break; } }
                if (served < 0) starved++; else { worst = Math.max(worst, served); best = Math.min(best, served); }
            }
            System.out.printf("  %-45s : starved in %2d of 20 runs (100000 ticks each)%s%n", label, starved, starved == 20 ? "" : String.format(", Old served after %d..%d ticks", best, worst));
        }
        STEP = 60;

        // ======== One simulated day: 5,000 patients ========
        System.out.println("\n==== DEPLOYMENT: one day with 5,000 patients (interleaved arrivals and treatments), avg of 20 days ====");
        double tot = 0;
        for (int d = -3; d < 20; d++) {
            Random r = new Random(d + 100); TriageQueue q = new TriageQueue(AGED); long t0 = System.nanoTime(); int admitted = 0, served = 0;
            while (served < 5000) { if (admitted < 5000 && (r.nextInt(3) != 0 || q.raw().isEmpty())) { q.admit("P" + admitted, 1 + r.nextInt(5)); admitted++; } else if (q.next() != null) served++; }
            if (d >= 0) tot += (System.nanoTime() - t0) / 1e6;
        }
        System.out.printf("  Total CPU time for admitting and serving all 5,000 patients: %.3f ms  (= %.1f us per patient)%n", tot / 20, tot / 20 * 1000 / 5000);
    }
}
