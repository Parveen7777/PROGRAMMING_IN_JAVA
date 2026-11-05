abstract class NewVehicle {
    private final String regNo;

    NewVehicle(String regNo) {
        this.regNo = regNo;
    }

    public String getRegNo() {
        return regNo;
    }

    public abstract double calculateFee(int minutes, int entryHour);
}

class EBike extends NewVehicle {
    EBike(String regNo) {
        super(regNo);
    }

    @Override
    public double calculateFee(int minutes, int entryHour) {
        return 20; // Flat parking fee
    }
}

class DeliveryVan extends NewVehicle {
    DeliveryVan(String regNo) {
        super(regNo);
    }

    @Override
    public double calculateFee(int minutes, int entryHour) {
        int hours = (minutes + 59) / 60;
        int rate = (entryHour >= 9 && entryHour < 17) ? 60 : 30;
        return hours * rate; // Double the car rate during 9 AM to 5 PM
    }
}

class SimpleJaggedParking {
    private final String[][] slots;

    SimpleJaggedParking(int[] slotsPerFloor) {
        slots = new String[slotsPerFloor.length][];
        for (int i = 0; i < slotsPerFloor.length; i++) {
            slots[i] = new String[slotsPerFloor[i]];
        }
    }

    public boolean park(String regNo) {
        for (int f = 0; f < slots.length; f++) {
            for (int s = 0; s < slots[f].length; s++) {
                if (slots[f][s] == null) {
                    slots[f][s] = regNo;
                    System.out.println(regNo + " parked at floor "
                            + (f + 1) + ", slot " + (s + 1));
                    return true;
                }
            }
        }
        return false;
    }
}

public class DesignChallenge {
    public static void main(String[] args) {
        SimpleJaggedParking parking =
                new SimpleJaggedParking(new int[] { 2, 3, 1, 2, 2 });

        EBike ebike = new EBike("TN10EB1234");
        DeliveryVan van = new DeliveryVan("TN20DV5678");

        parking.park(ebike.getRegNo());
        parking.park(van.getRegNo());

        System.out.println("E-bike flat fee: Rs "
                + ebike.calculateFee(90, 10));
        System.out.println("Delivery van fee at 10 AM: Rs "
                + van.calculateFee(60, 10));

        boolean visitorHasOneDayPass = true;
        double visitorFee = visitorHasOneDayPass
                ? 0 : van.calculateFee(60, 11);
        System.out.println("Visitor one-day pass fee: Rs " + visitorFee);
    }
}
