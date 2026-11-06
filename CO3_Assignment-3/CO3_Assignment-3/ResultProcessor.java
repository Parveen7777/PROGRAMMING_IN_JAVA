import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Checked exception for BAD CONTENT. Carries a category so the summary can count failure types. */
class ValidationException extends Exception {
    final String category;
    ValidationException(String category, String msg) { super(msg); this.category = category; }
    ValidationException(String category, String msg, Throwable cause) { super(msg, cause); this.category = category; }
}

/** Immutable outcome for ONE file (safe to share between threads). */
final class FileResult {
    final String file, category, message; final int total; final double percent; final boolean pass;
    private FileResult(String f, String c, String m, int t, double p, boolean ps) { file = f; category = c; message = m; total = t; percent = p; pass = ps; }
    static FileResult ok(String f, int total, boolean pass) { return new FileResult(f, "OK", "", total, total / 5.0, pass); }
    static FileResult failed(String f, String cat, String msg) { return new FileResult(f, cat, msg, 0, 0, false); }
    boolean success() { return category.equals("OK"); }
}

/** Stage 2 design: ExecutorService + Callable + Future + ConcurrentHashMap. */
public class ResultProcessor {
    static volatile int latencyMs = 0;               // simulated storage latency per file (0 = real disk only)
    static boolean guardUnexpected = true;           // false = naive collector (used only to demonstrate the failure)

    /** Reads + validates one file. Throws checked exceptions for expected problems. */
    static FileResult parse(Path p) throws IOException, ValidationException {
        String name = p.getFileName().toString();
        if (name.startsWith("poison")) throw new IllegalStateException("Injected fault (fault-injection test)");
        List<String> lines = Files.readAllLines(p);                         // NoSuchFileException if missing
        if (lines.stream().allMatch(String::isBlank)) throw new ValidationException("EMPTY", "file is empty");
        if (lines.get(0).isBlank()) throw new ValidationException("CORRUPT", "missing student id");
        if (lines.size() != 6) throw new ValidationException("CORRUPT", "expected 5 subject lines, found " + (lines.size() - 1));
        int total = 0; boolean pass = true;
        for (int i = 1; i <= 5; i++) {
            String[] part = lines.get(i).split(",");
            if (part.length != 2) throw new ValidationException("CORRUPT", "line " + (i + 1) + " is not SUBJECT,MARKS");
            int m;
            try { m = Integer.parseInt(part[1].trim()); }
            catch (NumberFormatException e) { throw new ValidationException("CORRUPT", "marks not a number on line " + (i + 1), e); }
            if (m < 0 || m > 100) throw new ValidationException("OUT_OF_RANGE", "marks " + m + " on line " + (i + 1));
            total += m; if (m < 40) pass = false;
        }
        return FileResult.ok(name, total, pass);
    }

    /** One unit of work. Expected failures become FileResult values; UNEXPECTED ones propagate into the Future. */
    static Callable<FileResult> task(Path p, ConcurrentMap<String, FileResult> results) {
        return () -> {
            if (latencyMs > 0) Thread.sleep(latencyMs);
            String name = p.getFileName().toString();
            FileResult r;
            try { r = parse(p); }
            catch (NoSuchFileException e)  { r = FileResult.failed(name, "MISSING", "file not found"); }
            catch (IOException e)          { r = FileResult.failed(name, "IO_ERROR", e.toString()); }
            catch (ValidationException e)  { r = FileResult.failed(name, e.category, e.getMessage()); }
            results.put(name, r);                                           // one key per file: no duplicates, no lost updates
            return r;
        };
    }

    /** Runs the whole batch with a fixed pool and returns the summary. */
    static Summary run(List<Path> files, int poolSize) {
        ConcurrentMap<String, FileResult> results = new ConcurrentHashMap<>();
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        List<Future<FileResult>> futures = new ArrayList<>(files.size());
        try {
            for (Path p : files) futures.add(pool.submit(task(p, results)));
            for (int i = 0; i < files.size(); i++) {
                String name = files.get(i).getFileName().toString();
                try { futures.get(i).get(60, TimeUnit.SECONDS); }
                catch (ExecutionException e) { if (guardUnexpected) results.putIfAbsent(name, FileResult.failed(name, "UNEXPECTED", e.getCause().toString())); }
                catch (TimeoutException e)   { futures.get(i).cancel(true); results.putIfAbsent(name, FileResult.failed(name, "TIMEOUT", "no answer in 60 s")); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
        } finally {
            pool.shutdownNow();
            try { pool.awaitTermination(5, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        if (guardUnexpected)                                                // completeness guarantee
            for (Path p : files) { String n = p.getFileName().toString(); results.putIfAbsent(n, FileResult.failed(n, "NOT_PROCESSED", "run interrupted")); }
        return new Summary(files.size(), results);
    }

    /** Stage 1 requirement: the summary is computed ONLY after every Future completed (happens-before via Future.get). */
    static class Summary {
        final int submitted; final TreeMap<String, Integer> byCategory = new TreeMap<>();
        int ok, passed; long sumTotals; double sumPercent; final int recorded;
        Summary(int submitted, Map<String, FileResult> r) {
            this.submitted = submitted; recorded = r.size();
            for (FileResult f : r.values()) {
                byCategory.merge(f.category, 1, Integer::sum);
                if (f.success()) { ok++; sumTotals += f.total; sumPercent += f.percent; if (f.pass) passed++; }
            }
        }
        int failed() { return recorded - ok; }
        boolean consistent() { return ok + failed() == submitted && recorded == submitted; }
        double meanPercent() { return ok == 0 ? 0 : sumPercent / ok; }
        void print() {
            System.out.println("  submitted=" + submitted + "  recorded=" + recorded + "  succeeded=" + ok + "  failed=" + failed()
                + "  -> summary consistent: " + consistent());
            System.out.println("  by category: " + byCategory);
            System.out.printf("  among succeeded: passed=%d  not passed=%d  mean%%=%.4f  sum of totals=%d%n", passed, ok - passed, meanPercent(), sumTotals);
        }
    }

    // ------------------------------------------------------------------ main
    static List<Path> manifest(Path dir, Path manifestFile) throws IOException {
        List<Path> l = new ArrayList<>();
        for (String s : Files.readAllLines(manifestFile)) if (!s.isBlank()) l.add(dir.resolve(s.trim()));
        return l;
    }
    public static void main(String[] args) throws Exception {
        System.out.println("=== STAGE 3 TEST 1: hand-checked set (11 manifest entries) ===");
        List<Path> small = manifest(Paths.get("data/small"), Paths.get("data/small_manifest.txt"));
        Summary s = run(small, 4);
        s.print();
        System.out.println("  HAND-CHECKED EXPECTATION: OK=6 (passed 4, not passed 2) CORRUPT=1 OUT_OF_RANGE=1 EMPTY=1 MISSING=1 UNEXPECTED=1, sum of totals=2015, mean%=67.1667");
        boolean match = s.ok == 6 && s.passed == 4 && s.sumTotals == 2015 && s.failed() == 5
            && s.byCategory.getOrDefault("CORRUPT", 0) == 1 && s.byCategory.getOrDefault("OUT_OF_RANGE", 0) == 1
            && s.byCategory.getOrDefault("EMPTY", 0) == 1 && s.byCategory.getOrDefault("MISSING", 0) == 1
            && s.byCategory.getOrDefault("UNEXPECTED", 0) == 1 && Math.abs(s.meanPercent() - 403.0 / 6) < 1e-9;
        System.out.println("  RESULT: " + (match ? "PASS - program summary equals the hand-checked values" : "FAIL"));

        System.out.println("\n=== STAGE 3 TEST 2: same set with a NAIVE collector (ignores ExecutionException) ===");
        guardUnexpected = false;
        Summary bad = run(small, 4);
        bad.print();
        System.out.println("  -> the injected crash silently disappeared; the summary is " + (bad.consistent() ? "consistent" : "INCONSISTENT (1 file unaccounted for)"));
        guardUnexpected = true;
    }
}
