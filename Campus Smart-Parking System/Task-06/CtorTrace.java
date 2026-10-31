abstract class V {
    private final String regNo;
    V(String regNo) { System.out.println("  3. Vehicle(regNo) body runs -> regNo = " + regNo); this.regNo = regNo; }
}
class C extends V {
    C(String regNo) { super(regNo); System.out.println("  4. Car(regNo) body runs"); }
}
class EC extends C {
    EC(String regNo) { super(regNo); System.out.println("  5. ElectricCar(regNo) body runs"); }
}
public class CtorTrace {
    public static void main(String[] args) {
        System.out.println("new ElectricCar(\"TN03C3\") ->");
        System.out.println("  (JVM allocates object, all fields = defaults; Object() runs first, silently)");
        new EC("TN03C3");
        System.out.println("  done: object fully built, reference returned to caller");
    }
}
