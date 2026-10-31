package parking.service;

import parking.model.*;
import java.time.LocalDateTime;

public class ParkingService {
    private static int totalOccupancy = 0;                // across ALL facilities (static)

    private final ParkingFacility facility;
    private final ParkingTicket[] active;                 // active tickets (array)
    private int activeCount = 0;
    private double collection = 0;

    public ParkingService(int[] slotsPerFloor) {
        facility = new ParkingFacility(slotsPerFloor);
        active = new ParkingTicket[facility.capacity()];
    }

    private int indexOf(String regNo) {                   // linear search: O(n)
        for (int i = 0; i < activeCount; i++)
            if (active[i].getVehicle().getRegNo().equals(regNo)) return i;
        return -1;
    }

    /** Backward-compatible overload used by the original tests. */
    public ParkingTicket enter(Vehicle v, UserType owner, boolean monthlyPass, LocalDateTime when) {
        return enter(v, owner, monthlyPass ? new MonthlyPass() : Pass.NONE, when);
    }

    public ParkingTicket enter(Vehicle v, UserType owner, Pass pass, LocalDateTime when) {
        if (indexOf(v.getRegNo()) >= 0)
            throw new IllegalStateException("Duplicate registration: " + v.getRegNo());
        int[] pos = facility.allocate(v.getRegNo());
        if (pos == null) throw new IllegalStateException("Facility full");
        ParkingTicket t = new ParkingTicket(v, owner, pass, pos[0], pos[1], when);
        active[activeCount++] = t;
        totalOccupancy++;
        return t;
    }

    public double exit(String regNo, LocalDateTime when, int kWh) {
        int i = indexOf(regNo.trim().toUpperCase());
        if (i < 0) throw new IllegalArgumentException("No active ticket for " + regNo);
        ParkingTicket t = active[i];
        Vehicle v = t.getVehicle();

        double parking = v.calculateFee(t.getEntry(), when);
        parking = t.getPass().adjustFee(parking, t.getOwner(), t.getEntry().toLocalDate());

        double charging = 0;
        if (kWh > 0) {
            if (v instanceof Chargeable c) charging = c.chargingFee(kWh);
            else throw new IllegalArgumentException(v + " cannot be charged");
        }
        t.setExit(when);                                      // mutate only after all checks pass
        facility.release(t.getFloor(), t.getSlot());
        active[i] = active[--activeCount];                // O(1) removal: move last into hole
        active[activeCount] = null;                       // let the ticket be garbage collected
        totalOccupancy--;
        double total = parking + charging;
        collection += total;
        return total;
    }

    public double getCollection() { return collection; }
    public int getActiveCount() { return activeCount; }
    public static int getTotalOccupancy() { return totalOccupancy; }
}
