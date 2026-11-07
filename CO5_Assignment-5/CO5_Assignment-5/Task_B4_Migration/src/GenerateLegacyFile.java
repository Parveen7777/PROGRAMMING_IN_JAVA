import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/** Creates a legacy bus-pass text file with n lines, about 1% of them deliberately damaged. */
public class GenerateLegacyFile {
    public static void main(String[] args) throws Exception {
        Path out = Path.of(args.length > 0 ? args[0] : "legacy_passes.csv");
        int n = args.length > 1 ? Integer.parseInt(args[1]) : 100000;
        int bad = 0;
        try (BufferedWriter w = Files.newBufferedWriter(out)) {
            for (int i = 1; i <= n; i++) {
                String id = String.format("P%07d", i);
                String line = String.join(",", id, "19231" + String.format("%04d", i % 10000),
                        "Student " + i, String.valueOf(1 + i % 30), String.valueOf(3000 + (i % 20) * 100.0));
                if (i % 100 == 0) {                         // every 100th line is damaged in a rotating way
                    switch ((i / 100) % 5) {
                        case 0 -> line = id + ",19231,TooFewFields";                         // wrong field count
                        case 1 -> line = String.join(",", id, "19231000", "BadAmt", "5", "abc"); // non-numeric amount
                        case 2 -> line = "";                                                  // empty line
                        case 3 -> line = String.join(",", id, "19231000", "BadRoute", "500", "3000.0"); // route out of range
                        default -> line = String.format("P%07d", Math.max(1, i - 1)) + ",19231000,DupId,5,3000.0"; // duplicate id
                    }
                    bad++;
                }
                w.write(line);
                w.newLine();
            }
        }
        System.out.println("Generated " + out + " with " + n + " lines, " + bad + " deliberately damaged");
    }
}
