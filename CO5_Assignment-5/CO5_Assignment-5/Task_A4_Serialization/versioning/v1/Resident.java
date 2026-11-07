import java.io.Serializable;
public class Resident implements Serializable {
    private static final long serialVersionUID = 1L;      // declared explicitly
    String name; String regNo;
    Resident(String n, String r) { name = n; regNo = r; }
    public String toString() { return "Resident[" + name + ", " + regNo + "]"; }
}
