class ParkingVehicle {
    private final String regNo;
    private final String type;
    private final double ratePerHour;

    ParkingVehicle(String regNo, String type, double ratePerHour) {
        this.regNo = regNo;
        this.type = type;
        this.ratePerHour = ratePerHour;
    }

    public String getRegNo() {
        return regNo;
    }

    public String getType() {
        return type;
    }

    public double calculateFee(int minutes) {
        if (minutes <= 0) {
            return 0;
        }
        int hours = (minutes + 59) / 60;
        return hours * ratePerHour;
    }
}

class ParkingTicket {
    private static int nextNumber = 1;
    private final int ticketNumber;
    private final ParkingVehicle vehicle;
    private final int slotNumber;

    ParkingTicket(ParkingVehicle vehicle, int slotNumber) {
        this.ticketNumber = nextNumber++;
        this.vehicle = vehicle;
        this.slotNumber = slotNumber;
    }

    public int getTicketNumber() {
        return ticketNumber;
    }

    public ParkingVehicle getVehicle() {
        return vehicle;
    }

    public int getSlotNumber() {
        return slotNumber;
    }
}

class ParkingFacilitySimple {
    private final ParkingTicket[] tickets;
    private final boolean[] occupied;

    ParkingFacilitySimple(int numberOfSlots) {
        tickets = new ParkingTicket[numberOfSlots];
        occupied = new boolean[numberOfSlots];
    }

    public ParkingTicket park(ParkingVehicle vehicle) {
        for (int i = 0; i < occupied.length; i++) {
            if (!occupied[i]) {
                occupied[i] = true;
                tickets[i] = new ParkingTicket(vehicle, i + 1);
                return tickets[i];
            }
        }
        return null;
    }

    public boolean leave(int slotNumber) {
        int index = slotNumber - 1;
        if (index < 0 || index >= occupied.length || !occupied[index]) {
            return false;
        }
        occupied[index] = false;
        tickets[index] = null;
        return true;
    }
}

public class SmartParkingSystem {
    public static void main(String[] args) {
        ParkingFacilitySimple facility = new ParkingFacilitySimple(2);

        ParkingVehicle bike = new ParkingVehicle("TN01AB1111", "TwoWheeler", 10);
        ParkingVehicle car = new ParkingVehicle("TN02CD2222", "Car", 30);
        ParkingVehicle extra = new ParkingVehicle("TN03EF3333", "Car", 30);

        ParkingTicket t1 = facility.park(bike);
        ParkingTicket t2 = facility.park(car);
        ParkingTicket t3 = facility.park(extra);

        System.out.println("Bike ticket: " + t1.getTicketNumber()
                + ", slot: " + t1.getSlotNumber());
        System.out.println("Car ticket: " + t2.getTicketNumber()
                + ", slot: " + t2.getSlotNumber());
        System.out.println("Third vehicle allocated: " + (t3 != null));

        double bikeFee = bike.calculateFee(61);
        double carFee = car.calculateFee(60);
        System.out.println("Bike fee for 61 minutes: Rs " + bikeFee);
        System.out.println("Car fee for 60 minutes: Rs " + carFee);
        System.out.println("Total collection: Rs " + (bikeFee + carFee));

        System.out.println("Release slot 1: " + facility.leave(1));
        System.out.println("Release slot 1 again: " + facility.leave(1));
    }
}
