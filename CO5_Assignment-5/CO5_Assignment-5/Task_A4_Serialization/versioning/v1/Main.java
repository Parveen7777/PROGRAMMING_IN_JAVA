import java.io.*;
public class Main { public static void main(String[] a) throws Exception {
    try (ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream("residents_v1.ser"))) { o.writeObject(new Resident("Arun", "192311001")); }
    System.out.println("v1 wrote residents_v1.ser (fields: name, regNo; serialVersionUID = 1L)"); } }
