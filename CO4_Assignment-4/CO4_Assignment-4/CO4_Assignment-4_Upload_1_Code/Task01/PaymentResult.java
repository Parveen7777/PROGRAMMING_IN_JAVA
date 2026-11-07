/** Immutable outcome returned by the service layer; the window only displays it. */
public record PaymentResult(boolean success, String message, String receiptNo) {
    public static PaymentResult ok(String receiptNo, String msg) { return new PaymentResult(true, msg, receiptNo); }
    public static PaymentResult fail(String msg) { return new PaymentResult(false, msg, null); }
}
