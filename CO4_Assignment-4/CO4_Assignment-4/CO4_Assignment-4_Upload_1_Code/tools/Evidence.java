import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Evidence generator: starts each GUI, performs real mouse clicks / key presses with java.awt.Robot and saves PNG screenshots.
 * Usage: java -cp <classes> Evidence <t1|t2|t3|t4|t5|t6> <outputFolder>
 * Needs a display (on a server: xvfb-run -a java ... ).
 */
public class Evidence {
    static Robot robot; static String out;

    public static void main(String[] a) throws Exception {
        robot = new Robot(); robot.setAutoDelay(40); out = a[1]; new File(out).mkdirs();
        switch (a[0]) { case "t1" -> t1(); case "t2" -> t2(); case "t3" -> t3(); case "t4" -> t4(); case "t5" -> t5(); case "t5b" -> t5b(a[2]); case "t6" -> t6(); default -> throw new IllegalArgumentException(a[0]); }
        System.exit(0);
    }

    // ---------------- helpers ----------------
    static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException e) { } }
    static void edt(Runnable r) throws Exception { EventQueue.invokeAndWait(r); }
    static void later(Runnable r) { EventQueue.invokeLater(r); }

    static <T extends Component> List<T> find(Container c, Class<T> k) {
        List<T> l = new ArrayList<>();
        for (Component x : c.getComponents()) { if (k.isInstance(x)) l.add(k.cast(x)); if (x instanceof Container y) l.addAll(find(y, k)); }
        if (c instanceof JFrame f && f.getJMenuBar() != null) l.addAll(find(f.getJMenuBar(), k));
        return l;
    }
    static Window win(String titlePart) {
        for (int i = 0; i < 40; i++) {
            for (Window w : Window.getWindows()) {
                String t = w instanceof Frame f ? f.getTitle() : w instanceof Dialog d ? d.getTitle() : "";
                if (w.isShowing() && t != null && t.contains(titlePart)) return w;
            }
            sleep(150);
        }
        throw new IllegalStateException("window not found: " + titlePart);
    }
    static Button awtButton(Container c, String label) { for (Button b : find(c, Button.class)) if (label.equals(b.getLabel())) return b; throw new IllegalStateException(label); }
    static JButton jButton(Container c, String text) { for (JButton b : find(c, JButton.class)) if (text.equals(b.getText())) return b; throw new IllegalStateException(text); }

    static void click(Component c) { click(c, c.getWidth() / 2, c.getHeight() / 2); }
    static void click(Component c, int dx, int dy) {
        Point p = c.getLocationOnScreen(); robot.mouseMove(p.x + dx, p.y + dy); sleep(120);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK); sleep(350);
    }
    static void key(int code) { robot.keyPress(code); robot.keyRelease(code); sleep(200); }
    static void shot(Window w, String name) throws Exception {
        sleep(500);
        Rectangle r = w.getBounds();
        BufferedImage img = robot.createScreenCapture(r);
        ImageIO.write(img, "png", new File(out, name + ".png"));
        System.out.println("saved " + name + ".png  (" + r.width + "x" + r.height + ")");
    }
    static void place(Window w, int x, int y) throws Exception { edt(() -> w.setLocation(x, y)); sleep(300); }

    // ---------------- Task 1 ----------------
    static void t1() throws Exception {
        FeeService svc = new FeeService(new InMemoryFeeRepository(), BankGateway.simulated(2500));
        FeeWindow[] fw = new FeeWindow[1];
        edt(() -> { fw[0] = new FeeWindow(svc); fw[0].setVisible(true); });
        Window w = win("Student Fee"); place(w, 60, 60);
        click(jButton(w, "Pay")); shot(w, "t1_1_invalid_input_blocked");                    // empty fields
        List<JTextField> f = find(w, JTextField.class);
        edt(() -> { f.get(0).setText("192211001"); f.get(1).setText("25000.50"); });
        click(jButton(w, "Pay")); sleep(300); shot(w, "t1_2_payment_in_progress");          // window still responsive
        sleep(2600); shot(w, "t1_3_payment_success");
        edt(() -> { f.get(0).setText("x'); DROP TABLE fees;--"); f.get(1).setText("10"); });
        click(jButton(w, "Pay")); shot(w, "t1_4_sql_injection_text_rejected");
        edt(() -> f.get(0).setText("192211001")); 
        later(() -> jButton(w, "Receipt").doClick()); sleep(800);
        Window d = win("Receipt"); place(d, 80, 220); shot(d, "t1_5_receipt_dialog");
    }

    // ---------------- Task 2 ----------------
    static void t2() throws Exception {
        AppointmentDesk[] d = new AppointmentDesk[1];
        edt(() -> { d[0] = new AppointmentDesk(); d[0].setVisible(true); });
        Window w = win("Appointment Desk"); place(w, 40, 40);
        shot(w, "t2_1_initial");
        edt(() -> { d[0].dept.select("Orthopaedics"); d[0].dept.dispatchEvent(new ItemEvent(d[0].dept, ItemEvent.ITEM_STATE_CHANGED, "Orthopaedics", ItemEvent.SELECTED)); });
        shot(w, "t2_2_department_changed_doctor_list_refreshed");
        edt(() -> d[0].patientId.setText("P10234")); click(d[0].book); shot(w, "t2_3_booking_confirmed");
        click(d[0].book); shot(w, "t2_4_error_slot_already_taken");
        edt(() -> { d[0].patientId.setText("12"); });  click(d[0].book); shot(w, "t2_5_error_invalid_patient_id");
        later(() -> d[0].dispatchEvent(new WindowEvent(d[0], WindowEvent.WINDOW_CLOSING))); sleep(900);
        Window dlg = win("Confirm"); place(dlg, 100, 300); shot(dlg, "t2_6_confirm_before_closing");
        click(awtButton(dlg, "No")); sleep(300);
        shot(w, "t2_7_after_cancelling_close");
    }

    // ---------------- Task 3 ----------------
    static void t3() throws Exception {
        LibrarySearch[] s = new LibrarySearch[1];
        edt(() -> { s[0] = new LibrarySearch(); s[0].setVisible(true); });
        Window w = win("Library - Search"); place(w, 20, 20);
        TextField q = find(w, TextField.class).get(0);
        edt(() -> q.setText("java")); click(awtButton(w, "Search"));
        shot(w, "t3_1_size_640x420_default");
        edt(() -> w.setSize(520, 300)); sleep(500); shot(w, "t3_2_size_520x300_small");
        edt(() -> w.setSize(980, 640)); sleep(500); shot(w, "t3_3_size_980x640_large");
        edt(() -> w.setSize(640, 420)); sleep(300);
        click(awtButton(w, "Next")); shot(w, "t3_4_next_page");
        java.awt.List list = find(w, java.awt.List.class).get(0);
        click(list, 100, 12); click(awtButton(w, "Reserve")); shot(w, "t3_5_reserve");
        edt(w::dispose);
        LibrarySearchGridBag[] g = new LibrarySearchGridBag[1];
        edt(() -> { g[0] = new LibrarySearchGridBag(); g[0].setVisible(true); });
        Window gw = win("GridBagLayout"); place(gw, 20, 20); shot(gw, "t3_6_gridbag_alternative");
    }

    // ---------------- Task 4 ----------------
    static void t4() throws Exception {
        DrawPadBuggy[] b = new DrawPadBuggy[1];
        edt(() -> { b[0] = new DrawPadBuggy(); });
        sleep(700);
        place(b[0], 30, 30);
        for (int[] p : new int[][]{{120, 120}, {220, 160}, {300, 100}}) click(b[0], p[0], p[1]);
        shot(b[0], "t4_1_buggy_nothing_drawn_count_stays_0");
        edt(b[0]::dispose);

        DrawPadHalfFixed[] h = new DrawPadHalfFixed[1];
        edt(() -> { h[0] = new DrawPadHalfFixed(); h[0].setVisible(true); });
        place(h[0], 30, 30);
        for (int[] p : new int[][]{{100, 110}, {200, 150}, {300, 90}, {250, 220}}) click(h[0], p[0], p[1]);
        shot(h[0], "t4_2_half_fixed_dots_drawn");
        edt(() -> h[0].setExtendedState(Frame.ICONIFIED)); sleep(900);
        edt(() -> h[0].setExtendedState(Frame.NORMAL)); sleep(1200);
        shot(h[0], "t4_3_half_fixed_after_minimise_restore_dots_lost");
        edt(h[0]::dispose);

        DrawPad[] f = new DrawPad[1];
        edt(() -> { f[0] = new DrawPad(); f[0].setVisible(true); });
        place(f[0], 30, 30);
        for (int[] p : new int[][]{{100, 110}, {200, 150}, {300, 90}, {250, 220}, {150, 200}}) click(f[0], p[0], p[1]);
        shot(f[0], "t4_4_fixed_before_minimise");
        edt(() -> f[0].setExtendedState(Frame.ICONIFIED)); sleep(900);
        edt(() -> f[0].setExtendedState(Frame.NORMAL)); sleep(1200);
        shot(f[0], "t4_5_fixed_after_minimise_restore_dots_survive");
    }

    // ---------------- Task 5 ----------------
    static void t5() throws Exception {
        Converter[] c = new Converter[1];
        edt(() -> { c[0] = new Converter(); c[0].setVisible(true); });
        Window w = win("Currency Converter"); place(w, 40, 40);
        JTextField t = find(w, JTextField.class).get(0);
        edt(() -> t.setText("5000")); click(jButton(w, "Convert")); shot(w, "t5_1_converter_valid_5000_INR_to_USD");
        edt(() -> t.setText("abc")); click(t); key(KeyEvent.VK_ENTER); shot(w, "t5_2_converter_invalid_amount_via_Enter_key");
        edt(() -> t.setText("5000")); key(KeyEvent.VK_ENTER); shot(w, "t5_3_converter_enter_key_valid");
        edt(w::dispose);
    }

    // Task 5.1 / 5.2: run one style (class name given), convert, screenshot. Folder v1 or v2 decides which classes are on the classpath.
    static void t5b(String cls) throws Exception {
        Class.forName(cls).getMethod("main", String[].class).invoke(null, (Object) new String[0]);
        Window w = win("Converter"); place(w, 40, 40);
        JTextField t = find(w, JTextField.class).get(0);
        edt(() -> t.setText("5000"));
        if (cls.endsWith("_Lambda") && out.contains("v2")) { click(t); key(KeyEvent.VK_ENTER); sleep(500); Window d = win("Message"); place(d, 60, 140); shot(d, "t5_" + cls + "_enter_key_dialog"); click(jButton(d, "OK")); }
        else { click(jButton(w, "Convert")); shot(w, "t5_" + cls + "_convert"); }
        if (cls.endsWith("_Lambda") && out.contains("v2")) { edt(() -> t.setText("abc")); click(jButton(w, "Convert")); Window d = win("Error"); place(d, 60, 140); shot(d, "t5_" + cls + "_invalid_dialog"); }
    }

    // ---------------- Task 6 ----------------
    static void t6() throws Exception {
        com.csa09.events.App.main(new String[0]);
        Window w = win("Campus Event"); place(w, 20, 20);
        sleep(900); shot(w, "t6_1_main_window_page1");
        JComboBox<?> filter = find(w, JComboBox.class).get(0);
        edt(() -> filter.setSelectedItem("Hackathon 2026")); sleep(900); shot(w, "t6_2_filter_by_event");
        edt(() -> filter.setSelectedIndex(0)); sleep(700);
        click(jButton(w, "Next >")); sleep(700); shot(w, "t6_3_next_page");
        // menu bar open
        JMenu file = ((JFrame) w).getJMenuBar().getMenu(0); click(file); shot(w, "t6_4_menu_bar_file_menu"); key(KeyEvent.VK_ESCAPE);
        // register dialog - invalid
        later(() -> jButton(w, "Register Student...").doClick()); Window d = win("Register for an event"); place(d, 120, 120);
        edt(() -> { find(d, JTextField.class).get(0).setText("12"); find(d, JTextField.class).get(1).setText("R2"); find(d, JTextField.class).get(2).setText("not-an-email"); });
        click(jButton(d, "Register")); shot(d, "t6_5_invalid_input_rejected");
        // valid
        edt(() -> { find(d, JTextField.class).get(0).setText("192299001"); find(d, JTextField.class).get(1).setText("Priya Nair"); find(d, JTextField.class).get(2).setText("priya.nair@example.com"); });
        shot(d, "t6_6_valid_form_filled");
        click(jButton(d, "Register")); sleep(1200); shot(w, "t6_7_after_successful_registration");
        // duplicate
        later(() -> jButton(w, "Register Student...").doClick()); Window d2 = win("Register for an event"); place(d2, 120, 120);
        edt(() -> { find(d2, JTextField.class).get(0).setText("192299001"); find(d2, JTextField.class).get(1).setText("Priya Nair"); find(d2, JTextField.class).get(2).setText("priya.nair@example.com"); });
        click(jButton(d2, "Register")); shot(d2, "t6_8_duplicate_registration_rejected");
        click(jButton(d2, "Cancel")); sleep(300);
        // same student, DIFFERENT event is allowed
        later(() -> jButton(w, "Register Student...").doClick()); Window d3 = win("Register for an event"); place(d3, 120, 120);
        edt(() -> { find(d3, JTextField.class).get(0).setText("192299001"); find(d3, JTextField.class).get(1).setText("Priya Nair"); find(d3, JTextField.class).get(2).setText("priya.nair@example.com");
                    @SuppressWarnings("unchecked") JComboBox<String> ev = (JComboBox<String>) find(d3, JComboBox.class).get(0); ev.setSelectedIndex(1); });
        shot(d3, "t6_9_same_student_other_event_allowed");
        click(jButton(d3, "Register")); sleep(1000);
        shot(w, "t6_10_after_second_event_registration");
    }
}
