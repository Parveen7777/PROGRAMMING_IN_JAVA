package parking.model;

import java.time.LocalDate;

/** Visitor pass: flat price for unlimited parking on one calendar day. */
public class OneDayPass implements Pass {
    public static final double PRICE = 100.0;
    private final LocalDate validOn;

    public OneDayPass(LocalDate validOn) {
        if (validOn == null) throw new IllegalArgumentException("Pass date required");
        this.validOn = validOn;
    }

    @Override public double adjustFee(double parkingFee, UserType user, LocalDate day) {
        return validOn.equals(day) ? PRICE : parkingFee;   // wrong-day pass gives no cover
    }
}
