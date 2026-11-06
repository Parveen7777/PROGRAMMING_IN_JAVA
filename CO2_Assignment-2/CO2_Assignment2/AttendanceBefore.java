import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

class StudentNoHash {
    private final String regNo;
    private final String name;
    StudentNoHash(String regNo, String name) { this.regNo = regNo; this.name = name; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudentNoHash)) return false;
        return regNo.equals(((StudentNoHash) o).regNo);
    }

    // TODO 1: hashCode() NOT overridden yet
    @Override public String toString() { return regNo + " " + name; }
}

public class AttendanceBefore {
    public static void main(String[] args) {
        Set<StudentNoHash> present = new HashSet<>();
        present.add(new StudentNoHash("192311001", "Arun")); // scanned at Gate 1
        present.add(new StudentNoHash("192311002", "Divya"));
        present.add(new StudentNoHash("192311001", "Arun")); // scanned again at Gate 3
        System.out.println("Unique students: " + present.size());
    }
}
