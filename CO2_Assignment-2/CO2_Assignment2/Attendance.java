import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

class Student {
    private final String regNo;
    private final String name;
    Student(String regNo, String name) { this.regNo = regNo; this.name = name; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;
        return regNo.equals(((Student) o).regNo);
    }
    // TODO 1 (completed): hashCode() uses exactly the field(s) used by equals()  -> regNo only
    @Override public int hashCode() { return Objects.hash(regNo); }

    @Override public String toString() { return regNo + " " + name; }
}

public class Attendance {
    public static void main(String[] args) {
        Set<Student> present = new HashSet<>();
        present.add(new Student("192311001", "Arun")); // scanned at Gate 1
        present.add(new Student("192311002", "Divya"));
        present.add(new Student("192311001", "Arun")); // scanned again at Gate 3
        System.out.println("Unique students: " + present.size());
    }
}
