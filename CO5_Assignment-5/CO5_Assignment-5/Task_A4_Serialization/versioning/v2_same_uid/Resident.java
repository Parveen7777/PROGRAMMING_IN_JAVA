import java.io.Serializable;
public class Resident implements Serializable {
    private static final long serialVersionUID = 1L;      // SAME id as v1 -> compatible change
    String name; String regNo; String phone;               // NEW field
    Resident(String n, String r) { name = n; regNo = r; }
    public String toString() { return "Resident[" + name + ", " + regNo + ", phone=" + phone + "]"; }
}
