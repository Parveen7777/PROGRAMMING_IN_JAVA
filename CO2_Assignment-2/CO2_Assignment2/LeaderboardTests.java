import java.util.*;

/** Q5 - tie-heavy data set + deliberately faulty comparators + a brute-force check of the comparison principle. */
public class LeaderboardTests {
    static class Team {
        final String name; final int points, goalDiff;
        Team(String n, int p, int g) { name = n; points = p; goalDiff = g; }
        @Override public String toString() { return name + "(" + points + "," + goalDiff + ")"; }
    }

    // ---- correct comparator ------------------------------------------------
    static final Comparator<Team> GOOD = Comparator.comparingInt((Team t) -> t.points).reversed()
            .thenComparing(Comparator.comparingInt((Team t) -> t.goalDiff).reversed())
            .thenComparing(t -> t.name);
    // ---- faulty comparators ------------------------------------------------
    static final Comparator<Team> F1_SUBTRACTION = (a, b) -> b.points - a.points != 0 ? b.points - a.points : (b.goalDiff - a.goalDiff != 0 ? b.goalDiff - a.goalDiff : a.name.compareTo(b.name));
    static final Comparator<Team> F2_NO_NAME     = Comparator.comparingInt((Team t) -> t.points).reversed().thenComparing(Comparator.comparingInt((Team t) -> t.goalDiff).reversed());
    static final Comparator<Team> F3_NEVER_ZERO  = (a, b) -> a.points > b.points ? -1 : 1;                        // never returns 0
    static final Comparator<Team> F4_CLOSE_IS_EQUAL = (a, b) -> Math.abs(a.points - b.points) <= 1 ? 0 : Integer.compare(b.points, a.points); // non-transitive "equality"

    /** checks reflexivity, antisymmetry and transitivity of compare() on every pair/triple. returns violations text. */
    static List<String> violations(List<Team> ts, Comparator<Team> c) {
        List<String> v = new ArrayList<>();
        for (Team a : ts) if (c.compare(a, a) != 0) { v.add("reflexivity: compare(" + a + "," + a + ") = " + c.compare(a, a) + " (must be 0)"); break; }
        outer:
        for (Team a : ts) for (Team b : ts)
            if (Integer.signum(c.compare(a, b)) != -Integer.signum(c.compare(b, a))) { v.add("antisymmetry: sgn compare(" + a + "," + b + ") != -sgn compare(" + b + "," + a + ")"); break outer; }
        int bad = 0; String ex = null;
        for (Team a : ts) for (Team b : ts) for (Team d : ts)
            if (c.compare(a, b) <= 0 && c.compare(b, d) <= 0 && c.compare(a, d) > 0) { bad++; if (ex == null) ex = a + " <= " + b + " <= " + d + " but " + a + " > " + d; }
            else if (c.compare(a, b) == 0 && c.compare(b, d) == 0 && c.compare(a, d) != 0) { bad++; if (ex == null) ex = a + " == " + b + " == " + d + " but compare(" + a + "," + d + ") != 0"; }
        if (bad > 0) v.add("transitivity: " + bad + " violating triples, e.g. " + ex);
        return v;
    }

    static void report(String name, List<Team> ts, Comparator<Team> c) {
        List<String> v = violations(ts, c);
        System.out.println("  " + name + ": " + (v.isEmpty() ? "OK - no violation of the principle" : "VIOLATES principle"));
        for (String s : v) System.out.println("      - " + s);
    }

    public static void main(String[] args) {
        // ---------- Data set 1: several ties at every level ----------
        List<Team> d1 = new ArrayList<>(List.of(
            new Team("Panthers", 9, 4), new Team("Eagles", 9, 6), new Team("Cobras", 6, 1), new Team("Bisons", 9, 4),
            new Team("Lions", 9, 6), new Team("Aces", 6, 1), new Team("Dragons", 6, -2), new Team("Falcons", 3, 0),
            new Team("Ants", 3, 0), new Team("Wolves", 6, 1)));
        System.out.println("==== DATA SET 1 (ties at points, goal-difference and name level) ====");
        System.out.println("Hand-worked expected order: Eagles, Lions (9,6 -> name); Bisons, Panthers (9,4); Aces, Cobras, Wolves (6,1); Dragons (6,-2); Ants, Falcons (3,0)");
        List<Team> sorted = new ArrayList<>(d1); sorted.sort(GOOD);
        System.out.println("Observed : " + sorted);
        String expected = "[Eagles(9,6), Lions(9,6), Bisons(9,4), Panthers(9,4), Aces(6,1), Cobras(6,1), Wolves(6,1), Dragons(6,-2), Ants(3,0), Falcons(3,0)]";
        System.out.println("Matches hand-worked order: " + sorted.toString().equals(expected));
        boolean stable = true; Random rnd = new Random(3);
        for (int i = 0; i < 1000; i++) { List<Team> sh = new ArrayList<>(d1); Collections.shuffle(sh, rnd); sh.sort(GOOD); if (!sh.toString().equals(expected)) stable = false; }
        System.out.println("Same result for 1000 random input permutations: " + stable);
        report("GOOD comparator on data set 1", d1, GOOD);

        // ---------- Data set 2: designed to break faulty comparators ----------
        List<Team> d2 = new ArrayList<>(List.of(
            new Team("Big", Integer.MAX_VALUE, 0), new Team("Neg", -10, 0), new Team("Mid", 50, 3), new Team("Mid2", 50, 3),
            new Team("Five", 5, 0), new Team("Six", 6, 0), new Team("Seven", 7, 0)));
        System.out.println("\n==== DATA SET 2 (designed to break faulty comparators) ====");
        System.out.println("Contains an extreme score (Integer.MAX_VALUE vs -10), identical teams, and points 5,6,7 (a 'close enough is equal' trap).");
        report("F1 points by subtraction", d2, F1_SUBTRACTION);
        System.out.println("      direct: F1.compare(Big, Neg) = " + F1_SUBTRACTION.compare(d2.get(0), d2.get(1)) + "  (positive => Big is placed AFTER Neg, though Big has far more points)");
        report("F2 ignores name tie-breaker", d2, F2_NO_NAME);
        List<Team> t1 = new ArrayList<>(d1), t2 = new ArrayList<>(d1); Collections.reverse(t2);
        t1.sort(F2_NO_NAME); t2.sort(F2_NO_NAME);
        System.out.println("      F2 on data set 1: input order A -> " + t1.subList(2, 4) + " ; reversed input -> " + t2.subList(2, 4) + "  => output depends on input order: " + !t1.equals(t2));
        report("F3 never returns 0", d2, F3_NEVER_ZERO);
        report("F4 'within 1 point = equal'", d2, F4_CLOSE_IS_EQUAL);
        // TimSort exception hunt with F3/F1 on bigger random data
        System.out.println("\n  Sorting 2000 random teams with faulty comparators (JDK may detect the broken contract):");
        List<Team> big = new ArrayList<>(); Random r = new Random(1);
        for (int i = 0; i < 2000; i++) big.add(new Team("T" + i, r.nextBoolean() ? Integer.MAX_VALUE - r.nextInt(5) : -r.nextInt(5) - 1, r.nextInt(3)));
        for (var e : Map.of("F1 subtraction", F1_SUBTRACTION, "F3 never zero", F3_NEVER_ZERO).entrySet()) {
            try { List<Team> c = new ArrayList<>(big); c.sort(e.getValue()); System.out.println("    " + e.getKey() + ": no exception, but order is " + (isSortedBy(c, GOOD) ? "correct (by luck)" : "WRONG")); }
            catch (IllegalArgumentException ex) { System.out.println("    " + e.getKey() + ": IllegalArgumentException: " + ex.getMessage()); }
        }
    }
    static boolean isSortedBy(List<Team> l, Comparator<Team> c) { for (int i = 1; i < l.size(); i++) if (c.compare(l.get(i - 1), l.get(i)) > 0) return false; return true; }
}
