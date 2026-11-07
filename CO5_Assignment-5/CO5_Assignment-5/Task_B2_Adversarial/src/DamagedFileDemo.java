import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** B2 - deliberately damaged bus-pass file and how the file store responds. */
public class DamagedFileDemo {
    public static void main(String[] args) throws Exception {
        Path f = Files.createTempFile("damaged", ".csv");
        Files.writeString(f, String.join("\n",
                "P2001,192311010,Asha,5,3900.0",              // 1 valid
                "P2002,192311011,Bala,5",                      // 2 wrong number of fields (4)
                "P2003,192311012,Charu,5,notanumber",          // 3 non-numeric amount
                "",                                            // 4 empty line
                "P2004,192311013,Dev,five,3900.0",             // 5 non-numeric route
                "P2005,192311014,Esha,5,3900.0,EXTRA,FIELDS",  // 6 too many fields
                ",192311015,NoId,5,3900.0",                    // 7 missing pass id
                "P2006,192311016,Farid,5,4100.0") + "\n");     // 8 valid
        PassStore s = new PassStore(f);

        System.out.println("File contents (8 lines): 2 valid, 6 damaged by design");
        int[] rep = s.scanReport();
        System.out.println("scanReport -> valid=" + rep[0] + ", damaged=" + rep[1]);
        for (String id : new String[]{"P2001", "P2002", "P2003", "P2004", "P2005", "P2006"}) {
            String[] r = s.find(id);
            System.out.println("find(" + id + ") -> " + (r == null ? "null (rejected/not found)" : String.join("|", r)));
        }
        List<String[]> route5 = s.listByRoute(5);
        System.out.println("listByRoute(5) returns " + route5.size() + " valid passes (no exception thrown)");
        Files.delete(f);
    }
}
