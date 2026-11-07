import java.io.Serializable;
public class Resident implements Serializable {
    // serialVersionUID NOT declared -> the JVM computes one from the class shape
    String name; String regNo; String phone;               // NEW field changes the computed id
    Resident(String n, String r) { name = n; regNo = r; }
    public String toString() { return "Resident[" + name + ", " + regNo + ", phone=" + phone + "]"; }
}
