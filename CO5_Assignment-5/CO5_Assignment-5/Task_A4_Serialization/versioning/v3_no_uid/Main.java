import java.io.*;
public class Main { public static void main(String[] a) throws Exception {
    try (ObjectInputStream i = new ObjectInputStream(new FileInputStream("residents_v1.ser"))) {
        System.out.println("read OK -> " + i.readObject());
    } catch (InvalidClassException e) { System.out.println("InvalidClassException -> " + e.getMessage()); } } }
