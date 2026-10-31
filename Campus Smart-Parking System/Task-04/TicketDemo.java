import parking.model.*;
import java.time.LocalDateTime;

public class TicketDemo {
    static void tryToReassign(ParkingTicket t, Vehicle v) { v = new Car("OTHER"); t.setExit(t.getEntry().plusHours(2)); }
    public static void main(String[] args) {
        LocalDateTime t0 = LocalDateTime.of(2026, 10, 30, 9, 0);
        Vehicle car = new Car("TN02B2");
        ParkingTicket a = new ParkingTicket(car, UserType.VISITOR, 0, 0, t0);
        ParkingTicket b = new ParkingTicket(car, UserType.VISITOR, 0, 1, t0);
        ParkingTicket copy = new ParkingTicket(a);
        System.out.println("a.ticketNo=" + a.getTicketNo() + "  b.ticketNo=" + b.getTicketNo() + "  copy.ticketNo=" + copy.getTicketNo());
        System.out.println("tickets issued (static counter) = " + ParkingTicket.ticketsIssued());
        System.out.println("a == copy : " + (a == copy) + "   same vehicle object : " + (a.getVehicle() == copy.getVehicle()));
        tryToReassign(a, car);
        System.out.println("after method call: car still = " + car + "   a.exit set by method = " + a.getExit());
        System.out.println("copy.exit (separate object) = " + copy.getExit());
    }
}
