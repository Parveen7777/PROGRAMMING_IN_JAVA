import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** A4 alternative - serialization vs CSV: file size and save/load time for n residents. */
public class SerializationBench {
    static class Resident implements Serializable {
        private static final long serialVersionUID = 1L;
        String name, regNo; int room;
        Resident(String n, String r, int room) { name = n; regNo = r; this.room = room; }
    }

    public static void main(String[] args) throws Exception {
        System.out.printf("%-10s %-14s %-12s %-12s %-14s %-12s %-12s%n",
                "n", "Serial size", "Ser save ms", "Ser load ms", "CSV size", "CSV save ms", "CSV load ms");
        for (int n : new int[]{1000, 10000, 100000}) {
            List<Resident> list = new ArrayList<>();
            for (int i = 0; i < n; i++) list.add(new Resident("Student " + i, "19231" + String.format("%05d", i), i % 500));
            Path ser = Files.createTempFile("res", ".ser"), csv = Files.createTempFile("res", ".csv");
            for (int w = 0; w < 2; w++) { // warm-up pass, then the measured pass
                long t = System.nanoTime();
                try (ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(ser)))) { o.writeObject(list); }
                double sSave = (System.nanoTime() - t) / 1e6;
                t = System.nanoTime();
                try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(ser)))) { in.readObject(); }
                double sLoad = (System.nanoTime() - t) / 1e6;
                t = System.nanoTime();
                try (BufferedWriter bw = Files.newBufferedWriter(csv)) {
                    for (Resident r : list) { bw.write(r.name + "," + r.regNo + "," + r.room); bw.newLine(); }
                }
                double cSave = (System.nanoTime() - t) / 1e6;
                t = System.nanoTime();
                List<Resident> back = new ArrayList<>();
                try (BufferedReader br = Files.newBufferedReader(csv)) {
                    String line;
                    while ((line = br.readLine()) != null) { String[] f = line.split(","); back.add(new Resident(f[0], f[1], Integer.parseInt(f[2]))); }
                }
                double cLoad = (System.nanoTime() - t) / 1e6;
                if (w == 1) System.out.printf("%-10d %-14d %-12.1f %-12.1f %-14d %-12.1f %-12.1f%n",
                        n, Files.size(ser), sSave, sLoad, Files.size(csv), cSave, cLoad);
            }
            Files.delete(ser); Files.delete(csv);
        }
    }
}
