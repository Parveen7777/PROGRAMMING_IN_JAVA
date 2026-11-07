import java.util.ArrayList;
import java.util.List;

/** Generates test passes: {passId, regNo, name, route, amount}. */
public class PassGenerator {
    public static List<String[]> generate(int n) {
        return generate(n, 0);
    }
    /** startId lets several threads / runs generate non-overlapping pass IDs. */
    public static List<String[]> generate(int n, int startId) {
        List<String[]> list = new ArrayList<>(n);
        for (int i = 1; i <= n; i++) {
            int id = startId + i;
            list.add(new String[]{
                    String.format("P%07d", id),
                    "19231" + String.format("%04d", id % 10000),
                    "Student " + id,
                    String.valueOf(1 + id % 30),
                    String.valueOf(3000 + (id % 20) * 100.0)});
        }
        return list;
    }
}
