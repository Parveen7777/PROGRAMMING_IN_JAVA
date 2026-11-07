/** Simple immutable doctor record. */
public record Doctor(String name, String department) {
    @Override public String toString() { return name; }
}
