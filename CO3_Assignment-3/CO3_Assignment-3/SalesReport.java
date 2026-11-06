import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;

/** Reader/Writer wrappers that count how many handles are opened and closed. */
class TrackedReader extends BufferedReader {
    static int opened = 0, closed = 0; private boolean done = false; final String name;
    TrackedReader(String f) throws FileNotFoundException { super(new FileReader(f)); name = f; opened++; }
    @Override public void close() throws IOException { if (!done) { done = true; closed++; } super.close(); }
    static void resetCounters() { opened = 0; closed = 0; }
    static int leaked() { return opened - closed; }
}
class TrackedWriter extends BufferedWriter {
    private boolean done = false;
    TrackedWriter(String f) throws IOException { super(new FileWriter(f)); TrackedReader.opened++; }
    @Override public void close() throws IOException { if (!done) { done = true; TrackedReader.closed++; } super.close(); }
}

public class SalesReport {
    static final Logger LOG = Logger.getLogger("SalesReport");
    static { LOG.setUseParentHandlers(false); Handler h = new StreamHandler(System.out, new SimpleFormatter() {
        @Override public String format(LogRecord r) { return "   [LOG " + r.getLevel() + "] " + r.getMessage() + "\n"; } }) {
        @Override public synchronized void publish(LogRecord r) { super.publish(r); flush(); } };
        LOG.addHandler(h); }

    /** Shared business logic: sum of qty*price. Throws NumberFormatException on a corrupt line. */
    static double total(BufferedReader r) throws IOException {
        r.readLine();                                   // header
        double sum = 0; String line;
        while ((line = r.readLine()) != null) {
            String[] p = line.split(",");
            sum += Integer.parseInt(p[1]) * Double.parseDouble(p[2]);
        }
        return sum;
    }

    // ---------------- Version 1: manual close(), BUGGY (close only on the success path) ----------------
    static void leaky(List<String> files, String out) throws IOException {
        BufferedWriter w = new TrackedWriter(out);
        double grand = 0;
        for (String f : files) {
            TrackedReader r = new TrackedReader(f);
            double t = total(r);                        // <-- exception here skips r.close() AND w.close()
            r.close();
            w.write(f + " total=" + String.format("%.2f", t) + "\n");
            grand += t;
        }
        w.write("GRAND TOTAL=" + String.format("%.2f", grand) + "\n");
        w.close();
    }

    // ---------------- Version 2: manual close() in finally (correct) ----------------
    static void manual(List<String> files, String out) throws IOException {
        BufferedWriter w = null;
        try {
            w = new TrackedWriter(out);
            double grand = 0;
            for (String f : files) {
                TrackedReader r = null;
                try {
                    r = new TrackedReader(f);
                    double t = total(r);
                    w.write(f + " total=" + String.format("%.2f", t) + "\n");
                    grand += t;
                } catch (NumberFormatException e) {
                    LOG.severe("Failed while reading file: " + f + " (" + e.getMessage() + ")");
                    w.write(f + " FAILED\n");
                } finally {
                    if (r != null) try { r.close(); } catch (IOException ignored) { }
                }
            }
            w.write("GRAND TOTAL=" + String.format("%.2f", grand) + "\n");
        } finally {
            if (w != null) try { w.close(); } catch (IOException ignored) { }
        }
    }

    // ---------------- Version 3: try-with-resources ----------------
    static void twr(List<String> files, String out) throws IOException {
        try (BufferedWriter w = new TrackedWriter(out)) {
            double grand = 0;
            for (String f : files) {
                try (TrackedReader r = new TrackedReader(f)) {
                    double t = total(r);
                    w.write(f + " total=" + String.format("%.2f", t) + "\n");
                    grand += t;
                } catch (NumberFormatException e) {
                    LOG.severe("Failed while reading file: " + f + " (" + e.getMessage() + ")");
                    w.write(f + " FAILED\n");
                }
            }
            w.write("GRAND TOTAL=" + String.format("%.2f", grand) + "\n");
        }
    }

    // ---------------- Version 4: no closing at all ----------------
    static void noClose(List<String> files, String out) throws IOException {
        BufferedWriter w = new TrackedWriter(out);
        for (String f : files) { TrackedReader r = new TrackedReader(f); w.write(f + " total=" + String.format("%.2f", total(r)) + "\n"); }
        w.flush();
    }

    interface Gen { void run(List<String> f, String o) throws IOException; }

    static void attempt(String label, Gen g, List<String> files, String out) {
        TrackedReader.resetCounters();
        String status = "completed normally";
        try { g.run(files, out); } catch (Exception e) { status = "ABORTED by " + e.getClass().getSimpleName(); }
        long size = new File(out).length();
        System.out.printf("%-26s status=%-34s handles opened=%d closed=%d LEAKED=%d report-bytes=%d%n",
            label, status, TrackedReader.opened, TrackedReader.closed, TrackedReader.leaked(), size);
    }

    static List<String> list(String dir, int n) { List<String> l = new ArrayList<>(); for (int i = 1; i <= n; i++) l.add(dir + "/store" + i + ".csv"); return l; }

    public static void main(String[] args) throws Exception {
        Files.createDirectories(Paths.get("out"));
        List<String> good = list("data/good", 4), bad = list("data/bad", 5);

        System.out.println("=== A. GOOD input (4 files): do all versions produce the same report? ===");
        attempt("v1 manual (buggy)", SalesReport::leaky,  good, "out/good_v1.txt");
        attempt("v2 manual finally", SalesReport::manual, good, "out/good_v2.txt");
        attempt("v3 try-with-resources", SalesReport::twr, good, "out/good_v3.txt");
        String r1 = Files.readString(Paths.get("out/good_v1.txt")), r2 = Files.readString(Paths.get("out/good_v2.txt")), r3 = Files.readString(Paths.get("out/good_v3.txt"));
        System.out.println("v1==v2: " + r1.equals(r2) + "   v2==v3: " + r2.equals(r3));
        System.out.println("--- report ---\n" + r3);

        System.out.println("=== B. FAILING input (store3.csv corrupt at line 5; store4, store5 come after it) ===");
        attempt("v1 manual (buggy)", SalesReport::leaky,  bad, "out/bad_v1.txt");
        attempt("v2 manual finally", SalesReport::manual, bad, "out/bad_v2.txt");
        attempt("v3 try-with-resources", SalesReport::twr, bad, "out/bad_v3.txt");
        System.out.println("--- v1 report file content (incomplete) ---\n" + Files.readString(Paths.get("out/bad_v1.txt")) + "<end>");
        System.out.println("--- v3 report file content ---\n" + Files.readString(Paths.get("out/bad_v3.txt")));

        System.out.println("=== C. No closing at all, f = 4 good files ===");
        attempt("v4 no close", SalesReport::noClose, good, "out/good_v4.txt");
        System.out.println("Expected leak = f + 1 writer = " + (4 + 1));
    }
}
