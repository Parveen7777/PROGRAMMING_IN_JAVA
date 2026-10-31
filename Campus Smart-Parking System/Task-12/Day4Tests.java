package parking.app;

import parking.model.*;
import parking.service.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Task 12 tests: new categories, one-day pass, 5 floors with different slot counts. */
public class Day4Tests {
    static LocalDateTime at(int h, int m) { return LocalDateTime.of(2026, 10, 30, h, m); }

    public static void main(String[] args) {
        VehicleFactory.register("EBIKE", EBike::new);               // new types plug in here
        VehicleFactory.register("DELIVERYVAN", DeliveryVan::new);
        LocalDate today = LocalDate.of(2026, 10, 30);

        TestRunner.check("Day4", "EBike 30 min = flat 20", 20.0, () -> new EBike("EB1").calculateFee(at(8, 0), at(8, 30)));
        TestRunner.check("Day4", "EBike 9 h = still flat 20", 20.0, () -> new EBike("EB1").calculateFee(at(8, 0), at(17, 0)));
        TestRunner.check("Day4", "EBike zero minutes", 0.0, () -> new EBike("EB1").calculateFee(at(8, 0), at(8, 0)));
        TestRunner.check("Day4", "Van 10:00-12:00 (peak) = 2 x 60", 120.0, () -> new DeliveryVan("V1").calculateFee(at(10, 0), at(12, 0)));
        TestRunner.check("Day4", "Van 07:00-09:00 (off-peak) = 2 x 30", 60.0, () -> new DeliveryVan("V1").calculateFee(at(7, 0), at(9, 0)));
        TestRunner.check("Day4", "Van 16:00-18:00 = 60 (peak) + 30", 90.0, () -> new DeliveryVan("V1").calculateFee(at(16, 0), at(18, 0)));
        TestRunner.check("Day4", "Van 08:30-09:30 = 1 block starting 08:30", 30.0, () -> new DeliveryVan("V1").calculateFee(at(8, 30), at(9, 30)));
        TestRunner.check("Day4", "Van 16:30-17:30 = block starts 16:30 (peak)", 60.0, () -> new DeliveryVan("V1").calculateFee(at(16, 30), at(17, 30)));
        TestRunner.check("Day4", "Van zero minutes", 0.0, () -> new DeliveryVan("V1").calculateFee(at(10, 0), at(10, 0)));
        TestRunner.check("Day4", "Visitor car 5 h without pass", 150.0, () -> {
            ParkingService s = new ParkingService(new int[]{2});
            s.enter(new Car("C1"), UserType.VISITOR, Pass.NONE, at(8, 0));
            return s.exit("C1", at(13, 0), 0); });
        TestRunner.check("Day4", "Visitor car 5 h with valid one-day pass", 100.0, () -> {
            ParkingService s = new ParkingService(new int[]{2});
            s.enter(new Car("C1"), UserType.VISITOR, new OneDayPass(today), at(8, 0));
            return s.exit("C1", at(13, 0), 0); });
        TestRunner.check("Day4", "One-day pass for another date gives no cover", 150.0, () -> {
            ParkingService s = new ParkingService(new int[]{2});
            s.enter(new Car("C1"), UserType.VISITOR, new OneDayPass(today.plusDays(1)), at(8, 0));
            return s.exit("C1", at(13, 0), 0); });
        TestRunner.check("Day4", "Monthly pass (staff) still free via Pass interface", 0.0, () -> {
            ParkingService s = new ParkingService(new int[]{2});
            s.enter(new Car("C1"), UserType.STAFF, new MonthlyPass(), at(8, 0));
            return s.exit("C1", at(13, 0), 0); });
        TestRunner.check("Day4", "Null OneDayPass date rejected", "IllegalArgumentException", () -> new OneDayPass(null));
        TestRunner.check("Day4", "5 floors, different slots: capacity 20+35+50+40+25", 170, () -> new ParkingFacility(new int[]{20, 35, 50, 40, 25}).capacity());
        TestRunner.check("Day4", "Factory builds new types by name", "DeliveryVan", () -> VehicleFactory.create("DeliveryVan", "DV9").getClass().getSimpleName());
        TestRunner.check("Day4", "Factory still rejects unknown type", "IllegalArgumentException", () -> VehicleFactory.create("Bus", "B1"));
        TestRunner.check("Day4", "Polymorphic mix: EBike+Van+Car in one service", 20.0 + 120.0 + 90.0, () -> {
            ParkingService s = new ParkingService(new int[]{20, 35, 50, 40, 25});
            s.enter(VehicleFactory.create("EBike", "EB1"), UserType.VISITOR, Pass.NONE, at(10, 0));
            s.enter(VehicleFactory.create("DeliveryVan", "DV1"), UserType.VISITOR, Pass.NONE, at(10, 0));
            s.enter(VehicleFactory.create("Car", "CA1"), UserType.VISITOR, Pass.NONE, at(10, 0));
            return s.exit("EB1", at(11, 0), 0) + s.exit("DV1", at(12, 0), 0) + s.exit("CA1", at(13, 0), 0); });
        System.out.println("SUMMARY|" + TestRunner.passed + "|" + TestRunner.n);
    }
}
