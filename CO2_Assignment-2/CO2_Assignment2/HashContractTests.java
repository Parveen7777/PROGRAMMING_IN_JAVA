import java.util.*;

/** Q3 - three deliberately designed tests of the hashCode-equals contract (prediction first, then result). */
public class HashContractTests {

    // equals() on regNo only, hashCode() NOT overridden  (the junior developer's class)
    static class BadStudent {
        final String regNo, name;
        BadStudent(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) { return o instanceof BadStudent && regNo.equals(((BadStudent) o).regNo); }
        @Override public String toString() { return regNo + " " + name; }
    }
    // corrected, immutable
    static class GoodStudent {
        final String regNo, name;
        GoodStudent(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) { return o instanceof GoodStudent && regNo.equals(((GoodStudent) o).regNo); }
        @Override public int hashCode() { return Objects.hash(regNo); }
        @Override public String toString() { return regNo + " " + name; }
    }
    // correct equals/hashCode but MUTABLE key field
    static class MutableStudent {
        String regNo; final String name;
        MutableStudent(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) { return o instanceof MutableStudent && regNo.equals(((MutableStudent) o).regNo); }
        @Override public int hashCode() { return Objects.hash(regNo); }
        @Override public String toString() { return regNo + " " + name; }
    }
    // legal but useless hashCode
    static class ConstHashStudent {
        final String regNo;
        ConstHashStudent(String r) { regNo = r; }
        @Override public boolean equals(Object o) { return o instanceof ConstHashStudent && regNo.equals(((ConstHashStudent) o).regNo); }
        @Override public int hashCode() { return 42; }
    }

    static void check(String what, int predicted, int observed) {
        System.out.println("  Prediction: " + predicted + "   Observed: " + observed + "   -> " + (predicted == observed ? "prediction CONFIRMED" : "prediction WRONG") + "  [" + what + "]");
    }

    public static void main(String[] args) {
        System.out.println("==== TEST 1: HashSet appears to work (same object re-scanned) ====");
        System.out.println("Scans: A(192311001) object, B(192311002) object, A (same object again). Expected unique = 2.");
        System.out.println("Prediction reasoning: the second add(A) passes the SAME object, so identity hashCode is identical and");
        System.out.println("equals() is true -> rejected as duplicate. Predicted size = 2 (looks correct, but only by luck of identity).");
        Set<BadStudent> t1 = new HashSet<>();
        BadStudent a = new BadStudent("192311001", "Arun");
        t1.add(a); t1.add(new BadStudent("192311002", "Divya")); t1.add(a);
        check("BadStudent, same object re-added", 2, t1.size());

        System.out.println("\n==== TEST 2: scans designed to expose duplicates (different objects, same student) ====");
        System.out.println("Scans: Arun x3 (3 different objects), Divya x2 (2 different objects). Expected unique = 2.");
        System.out.println("Prediction reasoning: Object.hashCode() is identity-based, so each new object gets a different hash and");
        System.out.println("lands in a different bucket; equals() is never even called. Predicted size = 5 (3 + 2).");
        Set<BadStudent> t2 = new HashSet<>();
        for (int i = 0; i < 3; i++) t2.add(new BadStudent("192311001", "Arun"));
        for (int i = 0; i < 2; i++) t2.add(new BadStudent("192311002", "Divya"));
        check("BadStudent, different objects", 5, t2.size());
        System.out.println("  contains(new BadStudent(\"192311002\",\"Divya\")) = " + t2.contains(new BadStudent("192311002", "Divya")) + "  (equal object NOT found)");
        System.out.println("  Contents: " + t2);
        Set<GoodStudent> t2g = new HashSet<>();
        for (int i = 0; i < 3; i++) t2g.add(new GoodStudent("192311001", "Arun"));
        for (int i = 0; i < 2; i++) t2g.add(new GoodStudent("192311002", "Divya"));
        System.out.println("  Same scans with corrected GoodStudent:");
        check("GoodStudent (equals+hashCode)", 2, t2g.size());

        System.out.println("\n==== TEST 3a: changing a hashCode field AFTER insertion (mutable key) ====");
        System.out.println("Prediction reasoning: object is stored in the bucket for hash(\"192311001\"). After regNo changes, hashCode");
        System.out.println("points to a different bucket, so contains()/remove() look in the wrong place -> false. size stays 1.");
        Set<MutableStudent> t3 = new HashSet<>();
        MutableStudent m = new MutableStudent("192311001", "Arun");
        t3.add(m);
        m.regNo = "192311999";
        System.out.println("  after m.regNo changed: size=" + t3.size() + ", contains(m)=" + t3.contains(m) + ", remove(m)=" + t3.remove(m) + ", size now=" + t3.size());
        System.out.println("  iteration still finds it: " + t3);
        t3.add(m);
        System.out.println("  add(m) again -> size=" + t3.size() + "  (same student now stored TWICE)");
        System.out.println("  Predicted contains=false, remove=false, size 1 -> then 2 : CONFIRMED by output above");
        m.regNo = "192311001";
        System.out.println("  after restoring regNo: contains(m)=" + t3.contains(m));

        System.out.println("\n==== TEST 3b: TreeSet whose compareTo() is inconsistent with equals() ====");
        System.out.println("Students (192311001,Arun) and (192311002,Arun): equals() says DIFFERENT (regNo differs).");
        System.out.println("TreeSet comparator compares NAME only -> compare()==0 -> treated as duplicate. Predicted: HashSet=2, TreeSet=1.");
        Set<GoodStudent> hs = new HashSet<>(); Set<GoodStudent> ts = new TreeSet<>(Comparator.comparing((GoodStudent s) -> s.name));
        for (Set<GoodStudent> s : List.of(hs, ts)) { s.add(new GoodStudent("192311001", "Arun")); s.add(new GoodStudent("192311002", "Arun")); }
        System.out.println("  HashSet size=" + hs.size() + ", TreeSet size=" + ts.size() + "  -> " + (hs.size() == 2 && ts.size() == 1 ? "prediction CONFIRMED" : "prediction WRONG"));

        System.out.println("\n==== TEST 3c: legal-but-poor hashCode() (constant 42) ====");
        System.out.println("Prediction: all objects share one bucket; each add() walks the chain -> O(n) per add, O(n^2) overall.");
        for (int w = 0; w < 3; w++) { Set<ConstHashStudent> c = new HashSet<>(); for (int i = 0; i < 3000; i++) c.add(new ConstHashStudent("W" + i)); } // JIT warm-up
        for (int n : new int[]{5000, 10000, 20000, 40000}) {
            long t0 = System.nanoTime(); Set<ConstHashStudent> c = new HashSet<>();
            for (int i = 0; i < n; i++) c.add(new ConstHashStudent("R" + i));
            double ms1 = (System.nanoTime() - t0) / 1e6;
            t0 = System.nanoTime(); Set<GoodStudent> g = new HashSet<>();
            for (int i = 0; i < n; i++) g.add(new GoodStudent("R" + i, "x"));
            double ms2 = (System.nanoTime() - t0) / 1e6;
            System.out.printf("  n=%5d  constant hash: %8.2f ms   proper hash: %6.2f ms   (sizes %d / %d)%n", n, ms1, ms2, c.size(), g.size());
        }
    }
}
