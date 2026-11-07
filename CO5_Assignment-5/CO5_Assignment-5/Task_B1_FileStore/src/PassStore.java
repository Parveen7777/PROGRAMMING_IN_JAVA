import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Bus-pass file store (text / CSV design from Task A1).
 * Record layout: passId,regNo,name,routeNo,amount   (one record per line)
 * Text fields are percent-encoded so that a comma or newline inside a name
 * can never be mistaken for a delimiter.
 */
public class PassStore {
    private final Path file;
    PassStore(Path file) { this.file = file; }

    // ---------- field encoding (handles delimiter inside a name) ----------
    static String enc(String s) {
        return s.replace("%", "%25").replace(",", "%2C")
                .replace("\n", "%0A").replace("\r", "%0D");
    }
    static String dec(String s) {
        return s.replace("%2C", ",").replace("%0A", "\n")
                .replace("%0D", "\r").replace("%25", "%");
    }

    /** Validates one line. Returns the five decoded fields, or null if the line is damaged. */
    static String[] parse(String line) {
        if (line == null || line.isEmpty()) return null;          // empty line
        String[] f = line.split(",", -1);
        if (f.length != 5) return null;                            // wrong number of fields
        if (f[0].isEmpty()) return null;                           // missing pass id
        try {
            Integer.parseInt(f[3]);                                // route must be an int
            Double.parseDouble(f[4]);                              // amount must be numeric
        } catch (NumberFormatException e) {
            return null;
        }
        f[2] = dec(f[2]);
        return f;
    }

    public void add(String passId, String regNo, String name, int route, double amount)
            throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(String.join(",", enc(passId), enc(regNo), enc(name),
                    String.valueOf(route), String.valueOf(amount)));
            w.newLine();
        }
    }

    /** Returns the five fields of the pass with the given ID, or null if not found. */
    public String[] find(String passId) throws IOException {
        // TODO 1 (completed): read line by line with a BufferedReader inside
        // try-with-resources; skip damaged lines; return the fields of the matching pass
        try (BufferedReader r = Files.newBufferedReader(file)) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] f = parse(line);
                if (f == null) continue;                 // damaged line: skip, keep searching
                if (f[0].equals(passId)) return f;
            }
        } catch (NoSuchFileException e) {
            return null;                                 // missing file = empty store
        }
        return null;
    }

    /** Operation (iii): all valid passes on a given route. */
    public List<String[]> listByRoute(int route) throws IOException {
        List<String[]> out = new ArrayList<>();
        try (BufferedReader r = Files.newBufferedReader(file)) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] f = parse(line);
                if (f != null && Integer.parseInt(f[3]) == route) out.add(f);
            }
        } catch (NoSuchFileException e) {
            // empty store
        }
        return out;
    }

    /** Returns {validLines, damagedLines}. */
    public int[] scanReport() throws IOException {
        int ok = 0, bad = 0;
        try (BufferedReader r = Files.newBufferedReader(file)) {
            String line;
            while ((line = r.readLine()) != null) {
                if (parse(line) == null) bad++; else ok++;
            }
        } catch (NoSuchFileException e) {
            // nothing
        }
        return new int[]{ok, bad};
    }

    public static void main(String[] args) throws IOException {
        Path f = Files.createTempFile("passes", ".csv");
        PassStore store = new PassStore(f);
        store.add("P1001", "192311001", "Arun", 12, 4500);
        Files.writeString(f, "P1002,DAMAGED LINE\n", StandardOpenOption.APPEND);
        store.add("P1003", "192311003", "Divya", 7, 3800);
        System.out.println(Arrays.toString(store.find("P1003")));
        System.out.println(Arrays.toString(store.find("P1002")));
        Files.delete(f);
    }
}
