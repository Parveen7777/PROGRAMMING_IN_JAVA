import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/** Three ways to count lines in a file (Task A2 / B1.3). */
public class LineCounter {

    /** Slow: one read() call (one system call) per byte. */
    public static long countUnbuffered(Path p) throws IOException {
        long lines = 0;
        try (InputStream in = new FileInputStream(p.toFile())) {
            int b;
            while ((b = in.read()) != -1) {
                if (b == '\n') lines++;
            }
        }
        return lines;
    }

    /** Fast: read into a byte[] buffer, so one system call fetches many bytes. */
    public static long countBuffered(Path p, int bufSize) throws IOException {
        long lines = 0;
        byte[] buf = new byte[bufSize];
        try (InputStream in = new FileInputStream(p.toFile())) {
            int n;
            while ((n = in.read(buf)) != -1) {
                for (int i = 0; i < n; i++) if (buf[i] == '\n') lines++;
            }
        }
        return lines;
    }

    /** Alternative concept: whole file in memory. O(file size) memory. */
    public static long countReadAllLines(Path p) throws IOException {
        return Files.readAllLines(p).size();
    }
}
