package parking.app;

import parking.model.*;
import parking.service.*;
import java.time.LocalDateTime;
import java.util.function.Supplier;

public class TestRunner {
    static int n = 0, passed = 0;
    static final LocalDateTime D = LocalDateTime.of(2026, 10, 30, 8, 0);

    static void check(String cat, String desc, Object expected, Supplier<Object> actual) {
        Object a;
        try { a = actual.get(); }
        catch (RuntimeException e) { a = e.getClass().getSimpleName(); }
        boolean ok = String.valueOf(expected).equals(String.valueOf(a));
        n++; if (ok) passed++;
        System.out.println("T" + String.format("%02d", n) + "|" + cat + "|" + desc + "|" + expected + "|" + a + "|" + (ok ? "PASS" : "FAIL"));
    }

    static LocalDateTime at(int h, int m) { return LocalDateTime.of(2026, 10, 30, h, m); }

    public static void main(String[] args) {
        // ---- Normal operation
        check("Normal", "TwoWheeler, 130 min", 30.0, () -> new TwoWheeler("TN01A1").calculateFee(130));
        check("Normal", "Car, 130 min", 90.0, () -> new Car("TN02B2").calculateFee(130));
        check("Normal", "ElectricCar parking + 10 kWh", 210.0,
              () -> new ElectricCar("TN03C3").calculateFee(130) + new ElectricCar("TN03C3").chargingFee(10));
        check("Normal", "Ticket numbers are unique and increasing", true, () -> {
            ParkingService s = new ParkingService(new int[]{2});
            ParkingTicket a = s.enter(new Car("A1"), UserType.VISITOR, false, D);
            ParkingTicket b = s.enter(new Car("A2"), UserType.VISITOR, false, D);
            return b.getTicketNo() == a.getTicketNo() + 1; });

        // ---- Boundary: fee slabs
        check("Boundary", "Zero-minute stay", 0.0, () -> new Car("X1").calculateFee(0));
        check("Boundary", "Exactly 60 min = 1 hour", 30.0, () -> new Car("X1").calculateFee(60));
        check("Boundary", "61 min = 2 hours", 60.0, () -> new Car("X1").calculateFee(61));
        check("Boundary", "480 min (8 h) = daily cap reached", 240.0, () -> new Car("X1").calculateFee(480));
        check("Boundary", "600 min capped at daily max", 240.0, () -> new Car("X1").calculateFee(600));
        check("Boundary", "1440 min = exactly one day", 240.0, () -> new Car("X1").calculateFee(1440));
        check("Boundary", "1441 min = day + 1 started hour", 270.0, () -> new Car("X1").calculateFee(1441));
        check("Boundary", "Integer-division trap avoided (130 min)", 90.0, () -> new Car("X1").calculateFee(130));

        // ---- Boundary: last free slot
        check("Boundary", "Last free slot is allocated", "[1, 0]", () -> {
            ParkingFacility f = new ParkingFacility(new int[]{2, 1});
            f.allocate("A"); f.allocate("B");
            return java.util.Arrays.toString(f.allocate("C")); });
        check("Boundary", "Entry when facility is full", "IllegalStateException", () -> {
            ParkingService s = new ParkingService(new int[]{1});
            s.enter(new Car("A1"), UserType.VISITOR, false, D);
            return s.enter(new Car("A2"), UserType.VISITOR, false, D); });
        check("Boundary", "Freed slot is re-used", "[0, 1]", () -> {
            ParkingFacility f = new ParkingFacility(new int[]{2, 1});
            f.allocate("A"); f.allocate("B"); f.release(0, 1);
            return java.util.Arrays.toString(f.allocate("C")); });

        // ---- Business rules
        check("Rule", "Student with monthly pass parks free", 0.0, () -> {
            ParkingService s = new ParkingService(new int[]{2});
            s.enter(new Car("S1"), UserType.STUDENT, true, at(8, 0));
            return s.exit("S1", at(13, 0), 0); });
        check("Rule", "Visitor claiming monthly pass still pays", 150.0, () -> {
            ParkingService s = new ParkingService(new int[]{2});
            s.enter(new Car("V1"), UserType.VISITOR, true, at(8, 0));
            return s.exit("V1", at(13, 0), 0); });
        check("Rule", "Pass holder still pays for EV charging", 60.0, () -> {
            ParkingService s = new ParkingService(new int[]{2});
            s.enter(new ElectricCar("E1"), UserType.STAFF, true, at(8, 0));
            return s.exit("E1", at(10, 0), 5); });
        check("Rule", "equals(): same regNo, different class", true, () -> new Car("TN02B2").equals(new ElectricCar("tn02b2")));
        check("Rule", "Slot is free again after exit", 1, () -> {
            ParkingService s = new ParkingService(new int[]{1});
            s.enter(new Car("A1"), UserType.VISITOR, false, at(8, 0));
            s.exit("A1", at(9, 0), 0);
            s.enter(new Car("A2"), UserType.VISITOR, false, at(9, 0));
            return s.getActiveCount(); });

        // ---- Invalid inputs
        check("Invalid", "Unknown vehicle type", "IllegalArgumentException", () -> VehicleFactory.create("HELICOPTER", "H1"));
        check("Invalid", "Duplicate registration number", "IllegalStateException", () -> {
            ParkingService s = new ParkingService(new int[]{3});
            s.enter(new Car("D1"), UserType.VISITOR, false, D);
            return s.enter(new TwoWheeler("d1"), UserType.VISITOR, false, D); });
        check("Invalid", "release(): floor out of range", false, () -> new ParkingFacility(new int[]{2}).release(5, 0));
        check("Invalid", "release(): slot out of range", false, () -> new ParkingFacility(new int[]{2}).release(0, 9));
        check("Invalid", "release(): slot already free", false, () -> new ParkingFacility(new int[]{2}).release(0, 0));
        check("Invalid", "Negative duration", "IllegalArgumentException", () -> new Car("X1").calculateFee(-5));
        check("Invalid", "Exit before entry", "IllegalArgumentException", () -> new Car("X1").calculateFee(at(10, 0), at(9, 0)));
        check("Invalid", "Exit for unknown registration", "IllegalArgumentException", () -> new ParkingService(new int[]{1}).exit("NOPE", D, 0));
        check("Invalid", "Charging a non-EV", "IllegalArgumentException", () -> {
            ParkingService s = new ParkingService(new int[]{1});
            s.enter(new Car("C1"), UserType.VISITOR, false, D);
            return s.exit("C1", at(9, 0), 5); });
        check("Invalid", "Blank registration number", "IllegalArgumentException", () -> new Car("  "));

        // ---- Hand-calculated scenario
        ParkingService s = new ParkingService(new int[]{4, 3});
        s.enter(VehicleFactory.create("TwoWheeler", "TN01A1"), UserType.STUDENT, true, at(8, 0));
        s.enter(VehicleFactory.create("Car", "TN02B2"), UserType.VISITOR, false, at(9, 0));
        s.enter(VehicleFactory.create("ElectricCar", "TN03C3"), UserType.VISITOR, false, at(10, 0));
        s.enter(VehicleFactory.create("Car", "TN04D4"), UserType.STAFF, true, at(8, 30));
        s.enter(VehicleFactory.create("TwoWheeler", "TN05E5"), UserType.VISITOR, false, at(8, 0));
        s.enter(VehicleFactory.create("TwoWheeler", "TN06F6"), UserType.VISITOR, false, at(8, 0));
        s.enter(VehicleFactory.create("ElectricCar", "TN07G7"), UserType.STAFF, true, at(9, 0));
        double[] each = {
            s.exit("TN01A1", at(11, 0), 0),
            s.exit("TN02B2", at(12, 10), 0),
            s.exit("TN03C3", at(11, 0), 10),
            s.exit("TN04D4", at(16, 0), 0),
            s.exit("TN05E5", at(8, 0), 0),
            s.exit("TN06F6", LocalDateTime.of(2026, 10, 31, 9, 0), 0),
            s.exit("TN07G7", at(11, 0), 5)};
        System.out.println("SCENARIO|" + java.util.Arrays.toString(each) + "|" + s.getCollection());
        check("Scenario", "Day's total collection (hand-calculated = 420)", 420.0, s::getCollection);
        check("Scenario", "Facility empty at end of day", 0, s::getActiveCount);
        System.out.println("SUMMARY|" + passed + "|" + n);
    }
}
