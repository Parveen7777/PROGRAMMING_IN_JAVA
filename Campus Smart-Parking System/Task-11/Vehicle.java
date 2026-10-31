package parking.model;

import java.time.Duration;
import java.time.LocalDateTime;

/** Abstract superclass of every vehicle. Holds state common to all vehicles. */
public abstract class Vehicle {
    public static final int MINUTES_PER_DAY = 24 * 60;
    public static final int DAILY_MAX_HOURS = 8;        // daily cap = 8 hours' charge

    private final String regNo;                         // never changes after creation

    protected Vehicle(String regNo) {
        if (regNo == null || regNo.isBlank())
            throw new IllegalArgumentException("Registration number is required");
        this.regNo = regNo.trim().toUpperCase();
    }

    public final String getRegNo() { return regNo; }

    /** Each concrete vehicle supplies its own hourly rate. */
    protected abstract double ratePerHour();

    protected double dailyMax() { return DAILY_MAX_HOURS * ratePerHour(); }

    /** Fee slabs: every started hour is charged in full; a day never costs more than dailyMax(). */
    public double calculateFee(int minutes) {                        // overload 1
        if (minutes < 0) throw new IllegalArgumentException("Duration cannot be negative");
        int fullDays = minutes / MINUTES_PER_DAY;
        int rest = minutes % MINUTES_PER_DAY;
        int hours = (rest + 59) / 60;                                // integer ceiling
        return fullDays * dailyMax() + Math.min(hours * ratePerHour(), dailyMax());
    }

    public double calculateFee(LocalDateTime in, LocalDateTime out) { // overload 2
        if (out.isBefore(in)) throw new IllegalArgumentException("Exit is before entry");
        return calculateFee(Math.toIntExact(Duration.between(in, out).toMinutes()));
    }

    @Override public boolean equals(Object o) {
        return o instanceof Vehicle v && regNo.equals(v.regNo);
    }
    @Override public int hashCode() { return regNo.hashCode(); }
    @Override public String toString() { return getClass().getSimpleName() + "[" + regNo + "]"; }
}
