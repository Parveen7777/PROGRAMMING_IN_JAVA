import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

/** Measures time for ONE repaint: redraw n stored ovals (O(n))  vs  copy one 400x300 image (O(W*H), independent of n). */
public class RepaintBenchmark {
    public static void main(String[] args) {
        BufferedImage target = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        BufferedImage cache = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Random r = new Random(1);
        System.out.printf("%-10s %-26s %-26s %-22s %-18s%n", "n clicks", "redraw points (us/repaint)", "copy image (us/repaint)", "points memory (KB)", "image memory (KB)");
        for (int n : new int[]{10, 100, 1_000, 10_000, 100_000}) {
            java.util.List<Point> pts = new ArrayList<>();
            Graphics2D cg = cache.createGraphics();
            for (int i = 0; i < n; i++) { Point p = new Point(r.nextInt(400), r.nextInt(300)); pts.add(p); cg.fillOval(p.x - 3, p.y - 3, 6, 6); }
            cg.dispose();
            int reps = Math.max(5, 200_000 / n);
            for (int w = 0; w < 3; w++) { redraw(target, pts); copy(target, cache); }
            long t1 = System.nanoTime(); for (int i = 0; i < reps; i++) redraw(target, pts); double a = (System.nanoTime() - t1) / 1e3 / reps;
            long t2 = System.nanoTime(); for (int i = 0; i < reps; i++) copy(target, cache);  double b = (System.nanoTime() - t2) / 1e3 / reps;
            System.out.printf("%-10d %-26.1f %-26.1f %-22d %-18d%n", n, a, b, n * 24L / 1024, 400L * 300 * 4 / 1024);
        }
    }
    static void redraw(BufferedImage t, java.util.List<Point> pts) { Graphics2D g = t.createGraphics(); for (Point p : pts) g.fillOval(p.x - 3, p.y - 3, 6, 6); g.dispose(); }
    static void copy(BufferedImage t, BufferedImage c) { Graphics2D g = t.createGraphics(); g.drawImage(c, 0, 0, null); g.dispose(); }
}
