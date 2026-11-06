import java.util.*;
import java.util.function.Function;

/** Q5 - reusable generic design (bounded types, wildcards), reuse for Player/College, and the alternatives. */
public class GenericDesign {

    // ---- reusable generic component -----------------------------------------
    static class Leaderboard<T> {
        private final List<T> items = new ArrayList<>();
        private final Comparator<? super T> order;                 // PECS: comparator CONSUMES T  -> ? super T
        Leaderboard(Comparator<? super T> order) { this.order = order; }
        void add(T item) { items.add(item); }
        void addAll(Collection<? extends T> more) { items.addAll(more); }   // PECS: collection PRODUCES T -> ? extends T
        List<T> ranking() { List<T> c = new ArrayList<>(items); c.sort(order); return c; }
        List<T> top(int k) { List<T> r = ranking(); return r.subList(0, Math.min(k, r.size())); }
        // bounded type parameter: only usable when T knows its own natural order
        static <E extends Comparable<? super E>> Leaderboard<E> natural() { return new Leaderboard<E>(Comparator.naturalOrder()); }
    }

    // ---- three unrelated domain types reuse the SAME class ----------------------
    record Team(String name, int points, int goalDiff) {}
    record Player(String name, int goals, int assists) {}
    record College(String name, int medals, int budgetRank) {}
    // Comparable version (one natural order, built into the class)
    record RankedTeam(String name, int points, int goalDiff) implements Comparable<RankedTeam> {
        public int compareTo(RankedTeam o) {
            int c = Integer.compare(o.points, points); if (c != 0) return c;
            c = Integer.compare(o.goalDiff, goalDiff); if (c != 0) return c;
            return name.compareTo(o.name);
        }
    }

    static long cmpCount;
    static <T> Comparator<T> counting(Comparator<T> c) { return (a, b) -> { cmpCount++; return c.compare(a, b); }; }
    static volatile long sink;

    public static void main(String[] args) {
        System.out.println("==== Reuse of Leaderboard<T> for three different types ====");
        Leaderboard<Team> lt = new Leaderboard<>(Comparator.comparingInt(Team::points).reversed().thenComparing(Comparator.comparingInt(Team::goalDiff).reversed()).thenComparing(Team::name));
        lt.addAll(List.of(new Team("Panthers", 9, 4), new Team("Eagles", 9, 6), new Team("Bisons", 9, 4)));
        Leaderboard<Player> lp = new Leaderboard<>(Comparator.comparingInt(Player::goals).reversed().thenComparing(Comparator.comparingInt(Player::assists).reversed()).thenComparing(Player::name));
        lp.addAll(List.of(new Player("Ravi", 5, 2), new Player("Asha", 5, 4), new Player("Kumar", 7, 0)));
        Leaderboard<College> lc = new Leaderboard<>(Comparator.comparingInt(College::medals).reversed().thenComparingInt(College::budgetRank).thenComparing(College::name));
        lc.addAll(List.of(new College("SIMATS", 12, 2), new College("IITM", 12, 1), new College("NITT", 8, 3)));
        System.out.println("Teams    : " + lt.ranking());
        System.out.println("Players  : " + lp.ranking());
        System.out.println("Colleges : " + lc.ranking());
        System.out.println("Top-2 players: " + lp.top(2));
        Leaderboard<RankedTeam> ln = Leaderboard.natural();         // bounded type E extends Comparable<? super E>
        ln.add(new RankedTeam("Panthers", 9, 4)); ln.add(new RankedTeam("Eagles", 9, 6)); ln.add(new RankedTeam("Bisons", 9, 4));
        System.out.println("Comparable-based (natural order): " + ln.ranking());
        // wildcard demonstration: a Comparator<Object> can rank Strings; List<? extends T> accepts subtype lists
        Leaderboard<String> ls = new Leaderboard<>(Comparator.<Object, String>comparing(Object::toString));
        List<String> names = List.of("b", "c", "a"); ls.addAll(names);
        System.out.println("Comparator<Object> accepted by Leaderboard<String> (? super T): " + ls.ranking());

        // ---- Alternative concept: three stable sorts vs one comparator chain vs Comparable ----
        System.out.println("\n==== Alternatives: sort time and number of comparisons (avg of 5 runs, after warm-up) ====");
        System.out.printf("%-10s %-34s %12s %16s %10s%n", "n", "approach", "avg ms", "comparisons", "same?");
        for (int n : new int[]{10_000, 100_000, 500_000}) {
            Random r = new Random(2); List<Team> base = new ArrayList<>(); List<RankedTeam> baseR = new ArrayList<>();
            for (int i = 0; i < n; i++) { int p = r.nextInt(30), g = r.nextInt(20) - 10; String nm = "T" + r.nextInt(n * 10); base.add(new Team(nm, p, g)); baseR.add(new RankedTeam(nm, p, g)); }
            Comparator<Team> chain = Comparator.comparingInt(Team::points).reversed().thenComparing(Comparator.comparingInt(Team::goalDiff).reversed()).thenComparing(Team::name);
            List<Team> ref = new ArrayList<>(base); ref.sort(chain);
            for (String ap : new String[]{"Comparator chain (1 sort)", "Comparable compareTo (1 sort)", "3 stable sorts (name,gd,points)"}) {
                double tot = 0; long cmps = 0; boolean same = true; int runs = n >= 500_000 ? 3 : 5;
                for (int run = -1; run < runs; run++) {
                    cmpCount = 0; long t0 = System.nanoTime(); List<Team> out;
                    if (ap.startsWith("Comparator")) { out = new ArrayList<>(base); out.sort(counting(chain)); }
                    else if (ap.startsWith("Comparable")) { List<RankedTeam> c = new ArrayList<>(baseR); c.sort(null); out = new ArrayList<>(); cmpCount = 0; for (RankedTeam x : c) out.add(new Team(x.name(), x.points(), x.goalDiff())); }
                    else { out = new ArrayList<>(base);
                           out.sort(counting(Comparator.comparing(Team::name)));
                           out.sort(counting(Comparator.comparingInt(Team::goalDiff).reversed()));
                           out.sort(counting(Comparator.comparingInt(Team::points).reversed())); }
                    double ms = (System.nanoTime() - t0) / 1e6;
                    if (run >= 0) { tot += ms; cmps = cmpCount; }
                    if (run == runs - 1) same = out.equals(ref);
                    sink += out.size();
                }
                System.out.printf("%-10d %-34s %12.2f %16s %10s%n", n, ap, tot / runs, ap.startsWith("Comparable") ? "n/a (not counted)" : String.valueOf(cmps), same);
            }
        }
    }
}
