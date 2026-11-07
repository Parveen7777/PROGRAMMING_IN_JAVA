import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

/** B1.1 - validation tests: normal case, missing id, damaged line, plus extra edge cases. */
public class PassStoreTest {
    static int pass = 0, fail = 0;

    static void check(String name, boolean ok, String detail) {
        System.out.printf("%-4s %-52s %s%n", ok ? "PASS" : "FAIL", name, detail);
        if (ok) pass++; else fail++;
    }

    public static void main(String[] args) throws Exception {
        Path f = Files.createTempFile("tests", ".csv");
        PassStore s = new PassStore(f);

        s.add("P1001", "192311001", "Arun", 12, 4500);
        s.add("P1003", "192311003", "Divya", 7, 3800);
        String[] r = s.find("P1003");
        check("T1 add then find existing pass", r != null && r[2].equals("Divya"), Arrays.toString(r));

        check("T2 find non-existent pass id", s.find("P9999") == null, "returned null");

        Files.writeString(f, "P1002,DAMAGED LINE\n", StandardOpenOption.APPEND);
        s.add("P1004", "192311004", "Meena", 12, 4000);
        check("T3a damaged line is not returned", s.find("P1002") == null, "returned null");
        check("T3b record AFTER damaged line still found", s.find("P1004") != null, Arrays.toString(s.find("P1004")));

        List<String[]> l = s.listByRoute(12);
        check("T4 list passes on route 12", l.size() == 2, "found " + l.size());
        check("T5 list passes on route with none (99)", s.listByRoute(99).isEmpty(), "empty list");

        s.add("P1005", "192311005", "Kumar, Ravi", 3, 2500);
        String[] k = s.find("P1005");
        check("T6 name containing a comma round-trips", k != null && k[2].equals("Kumar, Ravi"), Arrays.toString(k));

        int[] rep = s.scanReport();
        check("T7 scan report valid/damaged = 4/1", rep[0] == 4 && rep[1] == 1, "valid=" + rep[0] + " damaged=" + rep[1]);

        Files.delete(f);
        check("T8 missing file treated as empty store", s.find("P1001") == null, "no exception");

        System.out.println("\nSummary: " + pass + " passed, " + fail + " failed");
        if (fail > 0) System.exit(1);
    }
}
