import java.time.Duration;

public class CastDemo {
    public static void main(String[] args) {
        int minutes = 130;
        double rate = 30;
        System.out.println("1. minutes / 60 * rate            = " + (minutes / 60 * rate));
        System.out.println("2. (double)(minutes / 60) * rate  = " + ((double) (minutes / 60) * rate));
        System.out.println("3. minutes / 60.0 * rate          = " + (minutes / 60.0 * rate));
        System.out.println("4. Math.ceil(minutes / 60) * rate = " + (Math.ceil(minutes / 60) * rate));
        System.out.println("5. (minutes + 59) / 60 * rate     = " + ((minutes + 59) / 60 * rate));
        long big = 3_000_000_000L;
        System.out.println("6. (int) 3_000_000_000L           = " + (int) big);
        try { Math.toIntExact(big); } catch (ArithmeticException e) { System.out.println("   Math.toIntExact(3_000_000_000L) -> " + e.getMessage()); }
        Integer a = 127, b = 127, c = 128, d = 128;
        System.out.println("7. Integer 127 == 127 : " + (a == b) + "   Integer 128 == 128 : " + (c == d) + "   128 equals 128 : " + c.equals(d));
    }
}
