import java.util.*;
import java.util.concurrent.*;

/** Common work: scan 'scripts' answer scripts, ~5 ms each (+ random jitter), record finish order. */
class Work {
    static final Queue<String> finishOrder = new ConcurrentLinkedQueue<>();
    static void scan(String station, int scripts, int[] result, int slot) {
        Random rnd = new Random();
        int done = 0;
        try {
            for (int i = 0; i < scripts; i++) { Thread.sleep(4 + rnd.nextInt(3)); done++; }
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        result[slot] = done;                                   // each station writes ONLY its own slot
        finishOrder.add(station);
        System.out.println("  Station " + station + " finished: " + done + " scripts [" + Thread.currentThread().getName() + "]");
    }
}

/** Way 1: extend Thread */
class StationThread extends Thread {
    private final int scripts, slot; private final int[] result;
    StationThread(String name, int scripts, int[] result, int slot) { super(name); this.scripts = scripts; this.result = result; this.slot = slot; }
    @Override public void run() { Work.scan(getName(), scripts, result, slot); }
}

/** Way 2: implement Runnable (preferred: task separated from thread, class can still extend something else) */
class StationRunnable implements Runnable {
    private final String name; private final int scripts, slot; private final int[] result;
    StationRunnable(String name, int scripts, int[] result, int slot) { this.name = name; this.scripts = scripts; this.result = result; this.slot = slot; }
    @Override public void run() { Work.scan(name, scripts, result, slot); }
}

public class Scanning {
    static final String[] NAMES = {"S1", "S2", "S3", "S4"};
    static final int[] BATCH = {30, 32, 31, 33};

    static void report(String title, Thread[] ts, int[] result) throws InterruptedException {
        Work.finishOrder.clear();
        System.out.println("=== " + title + " ===");
        for (Thread t : ts) System.out.println("  " + t.getName() + " state before start(): " + t.getState() + " isAlive=" + t.isAlive());
        for (Thread t : ts) t.start();
        for (Thread t : ts) System.out.println("  " + t.getName() + " state right after start(): " + t.getState() + " isAlive=" + t.isAlive());
        Thread.sleep(20);
        for (Thread t : ts) System.out.println("  " + t.getName() + " state at +20 ms: " + t.getState() + " isAlive=" + t.isAlive());
        for (Thread t : ts) t.join();                           // main waits here
        for (Thread t : ts) System.out.println("  " + t.getName() + " state after join(): " + t.getState() + " isAlive=" + t.isAlive());
        int total = 0; for (int r : result) total += r;
        System.out.println("  FINISH ORDER: " + Work.finishOrder);
        System.out.println("  TOTAL scanned = " + total + " (expected 126)\n");
    }

    public static void main(String[] args) throws Exception {
        int[] r1 = new int[4];
        Thread[] a = new Thread[4];
        for (int i = 0; i < 4; i++) a[i] = new StationThread(NAMES[i], BATCH[i], r1, i);
        report("Version 1: extends Thread", a, r1);

        int[] r2 = new int[4];
        Thread[] b = new Thread[4];
        for (int i = 0; i < 4; i++) b[i] = new Thread(new StationRunnable(NAMES[i], BATCH[i], r2, i), NAMES[i]);
        report("Version 2: implements Runnable", b, r2);
    }
}
