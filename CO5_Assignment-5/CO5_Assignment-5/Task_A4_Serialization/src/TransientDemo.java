import java.io.*;
import java.util.ArrayList;
import java.util.List;

/** A4.1 - what happens to the object graph, and why password / connection must be transient. */
public class TransientDemo {
    static class FakeConnection { }   // stands in for java.sql.Connection (NOT Serializable)

    // WRONG design: password stored, live connection not transient
    static class BadResident implements Serializable {
        private static final long serialVersionUID = 1L;
        String name, regNo, password; FakeConnection con = new FakeConnection();
        BadResident(String n, String r, String p) { name = n; regNo = r; password = p; }
    }
    // RIGHT design
    static class Resident implements Serializable {
        private static final long serialVersionUID = 1L;
        String name, regNo;
        transient String password;            // never written to disk
        transient FakeConnection con = new FakeConnection();   // live resource: cannot / must not be saved
        Resident(String n, String r, String p) { name = n; regNo = r; password = p; }
    }
    static class Room implements Serializable { private static final long serialVersionUID = 1L;
        int no; List<Object> residents = new ArrayList<>(); Room(int no) { this.no = no; } }
    static class Hostel implements Serializable { private static final long serialVersionUID = 1L;
        List<Room> rooms = new ArrayList<>(); }

    static byte[] write(Object o) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try (ObjectOutputStream oo = new ObjectOutputStream(bo)) { oo.writeObject(o); }
        return bo.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        Hostel bad = new Hostel(); Room r1 = new Room(101);
        r1.residents.add(new BadResident("Arun", "192311001", "pw#1")); bad.rooms.add(r1);
        try { write(bad); System.out.println("BAD design serialized?!"); }
        catch (NotSerializableException e) {
            System.out.println("1. Non-transient connection -> NotSerializableException: " + e.getMessage());
        }

        Hostel good = new Hostel(); Room r = new Room(101);
        r.residents.add(new Resident("Arun", "192311001", "pw#1"));
        r.residents.add(new Resident("Divya", "192311003", "pw#2"));
        good.rooms.add(r); good.rooms.add(new Room(102));
        byte[] data = write(good);
        System.out.println("2. Whole graph Hostel -> Room -> Resident written in " + data.length + " bytes (one writeObject call)");
        try (ObjectInputStream oi = new ObjectInputStream(new ByteArrayInputStream(data))) {
            Hostel back = (Hostel) oi.readObject();
            Resident x = (Resident) back.rooms.get(0).residents.get(0);
            System.out.println("3. After reading back: name=" + x.name + ", regNo=" + x.regNo
                    + ", password=" + x.password + ", connection=" + x.con + "   (transient fields reset to null)");
        }
        // shared references are preserved
        Resident shared = new Resident("Shared", "1", "p");
        Room a = new Room(1), b = new Room(2); a.residents.add(shared); b.residents.add(shared);
        Hostel h = new Hostel(); h.rooms.add(a); h.rooms.add(b);
        try (ObjectInputStream oi = new ObjectInputStream(new ByteArrayInputStream(write(h)))) {
            Hostel back = (Hostel) oi.readObject();
            System.out.println("4. Same Resident in two rooms is still ONE object after reload: "
                    + (back.rooms.get(0).residents.get(0) == back.rooms.get(1).residents.get(0)));
        }
    }
}
