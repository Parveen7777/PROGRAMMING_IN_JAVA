interface Chargeable {
    double chargingFee(int kWh);
}

abstract class Vehicle {
    private final String regNo;

    protected Vehicle(String regNo) {
        this.regNo = regNo;
    }

    public String getRegNo() {
        return regNo;
    }

    protected abstract double ratePerHour();

    // Every started hour is charged in full.
    public double calculateFee(int minutes) {
        if (minutes <= 0) {
            return 0;
        }
        int hours = (minutes + 59) / 60;
        return hours * ratePerHour();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Vehicle)) {
            return false;
        }
        Vehicle other = (Vehicle) o;
        return regNo.equals(other.regNo);
    }

    @Override
    public int hashCode() {
        return regNo.hashCode();
    }
}

class TwoWheeler extends Vehicle {
    TwoWheeler(String regNo) {
        super(regNo);
    }

    @Override
    protected double ratePerHour() {
        return 10;
    }
}

class Car extends Vehicle {
    Car(String regNo) {
        super(regNo);
    }

    @Override
    protected double ratePerHour() {
        return 30;
    }
}

class ElectricCar extends Car implements Chargeable {
    ElectricCar(String regNo) {
        super(regNo);
    }

    @Override
    public double chargingFee(int kWh) {
        return kWh * 12;
    }
}

public class FeeDemo {
    public static void main(String[] args) {
        Vehicle[] parked = {
            new TwoWheeler("TN01A1"),
            new Car("TN02B2"),
            new ElectricCar("TN03C3")
        };

        for (Vehicle v : parked) {
            double fee = v.calculateFee(130);
            if (v instanceof Chargeable) {
                Chargeable c = (Chargeable) v;
                fee += c.chargingFee(10);
            }
            System.out.println(v.getClass().getSimpleName() + " "
                    + v.getRegNo() + " -> Rs " + fee);
        }

        System.out.println(new Car("TN02B2").equals(
                new ElectricCar("TN02B2")));
    }
}
