import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Hospital Outpatient Appointment Desk (AWT).
 * Events handled: ItemEvent (department/doctor change), ActionEvent (Book / Clear), WindowEvent (open/close).
 * Doctors are loaded ONCE into Map<String, List<Doctor>>, so a department change costs O(m_d) with no I/O.
 */
public class AppointmentDesk extends Frame implements ItemListener, ActionListener {
    // ---------- model ----------
    private final Map<String, List<Doctor>> doctorsByDept = new LinkedHashMap<>();
    private final Set<String> booked = new HashSet<>();            // key: doctor|slot
    private static final String[] SLOTS = {"09:00", "09:30", "10:00", "10:30", "11:00", "11:30"};

    // ---------- view (package-private so TraceDemo can drive them) ----------
    final TextField patientId = new TextField(14);
    final Choice dept = new Choice();
    final Choice doctor = new Choice();
    final Choice slot = new Choice();
    final Button book = new Button("Book Appointment");
    final Button clear = new Button("Clear");
    final Label status = new Label("Ready.");
    final TextArea trace = new TextArea(7, 52);
    boolean bookingInProgress = false;                              // true while a form is partly filled / not yet booked
    Confirmer confirmer = this::askYesNo;                           // replaceable in tests

    interface Confirmer { boolean confirm(String message); }

    public AppointmentDesk() {
        super("Outpatient Appointment Desk");
        loadDoctors();
        setLayout(new BorderLayout(6, 6));
        Panel form = new Panel(new GridLayout(5, 2, 6, 6));
        form.add(new Label("Patient ID:"));  form.add(patientId);
        form.add(new Label("Department:"));  form.add(dept);
        form.add(new Label("Doctor:"));      form.add(doctor);
        form.add(new Label("Time slot:"));   form.add(slot);
        form.add(book);                      form.add(clear);
        for (String d : doctorsByDept.keySet()) dept.add(d);
        for (String s : SLOTS) slot.add(s);
        refreshDoctors();                                            // initial fill for the first department
        Panel south = new Panel(new BorderLayout());
        status.setForeground(new Color(0x1B5E20));
        south.add(status, BorderLayout.NORTH); south.add(trace, BorderLayout.CENTER);
        trace.setEditable(false);
        add(new Label("  Book an outpatient appointment", Label.LEFT), BorderLayout.NORTH);
        add(form, BorderLayout.CENTER); add(south, BorderLayout.SOUTH);

        dept.addItemListener(this);                                  // ItemEvent  -> itemStateChanged
        doctor.addItemListener(this);
        book.addActionListener(this);                                // ActionEvent-> actionPerformed
        clear.addActionListener(this);
        patientId.addTextListener(e -> bookingInProgress = true);    // TextEvent: a booking has started
        addWindowListener(new WindowAdapter() {                      // WindowEvent -> window-event methods
            @Override public void windowOpened(WindowEvent e)      { log("windowOpened"); }
            @Override public void windowActivated(WindowEvent e)   { log("windowActivated"); }
            @Override public void windowDeactivated(WindowEvent e) { log("windowDeactivated"); }
            @Override public void windowClosing(WindowEvent e) {
                log("windowClosing");
                if (bookingInProgress && !confirmer.confirm("A booking is in progress. Close anyway?")) { log("close cancelled"); return; }
                dispose();
            }
            @Override public void windowClosed(WindowEvent e)      { log("windowClosed"); }
        });
        pack();
    }

    private void loadDoctors() {                                     // would be a one-time DB load in the real system
        put("Cardiology", "Dr. Ananya Rao", "Dr. Vikram Shah");
        put("Orthopaedics", "Dr. Meera Iyer", "Dr. Karthik N");
        put("Paediatrics", "Dr. Sana Khan", "Dr. R. Prakash", "Dr. Lakshmi V");
    }
    private void put(String d, String... names) {
        List<Doctor> l = new ArrayList<>();
        for (String n : names) l.add(new Doctor(n, d));
        doctorsByDept.put(d, l);
    }

    /** (a) Refresh the doctor list when the department changes. */
    void refreshDoctors() {
        doctor.removeAll();
        for (Doctor d : doctorsByDept.getOrDefault(dept.getSelectedItem(), List.of())) doctor.add(d.name());
    }

    @Override public void itemStateChanged(ItemEvent e) {
        log("itemStateChanged(" + (e.getSource() == dept ? "department" : "doctor") + " = " + e.getItem() + ")");
        if (e.getSource() == dept) refreshDoctors();
        bookingInProgress = true;
    }

    @Override public void actionPerformed(ActionEvent e) {
        log("actionPerformed(" + e.getActionCommand() + ")");
        if (e.getSource() == clear) { patientId.setText(""); bookingInProgress = false; show("Form cleared.", false); }
        else if (e.getSource() == book) bookAppointment();
    }

    /** (b) Validate and book. */
    void bookAppointment() {
        String id = patientId.getText().trim();
        if (!id.matches("P\\d{5}"))               { show("Error: Patient ID must look like P12345.", true); return; }
        if (doctor.getItemCount() == 0)           { show("Error: no doctor available for this department.", true); return; }
        String key = doctor.getSelectedItem() + "|" + slot.getSelectedItem();
        if (!booked.add(key))                     { show("Error: slot already taken - choose another slot.", true); return; }
        bookingInProgress = false;
        show("Booked: " + id + " with " + doctor.getSelectedItem() + " at " + slot.getSelectedItem(), false);
    }

    private void show(String msg, boolean error) { status.setForeground(error ? Color.RED : new Color(0x1B5E20)); status.setText(msg); }
    void log(String m) { System.out.println("[event] " + m); trace.append(m + "\n"); }

    private boolean askYesNo(String msg) {
        final boolean[] result = {false};
        final Dialog d = new Dialog(this, "Confirm", true);
        d.setLayout(new FlowLayout());
        d.add(new Label(msg));
        Button yes = new Button("Yes"), no = new Button("No");
        yes.addActionListener(e -> { result[0] = true; d.dispose(); });
        no.addActionListener(e -> d.dispose());
        d.add(yes); d.add(no); d.pack(); d.setLocationRelativeTo(this); d.setVisible(true);
        return result[0];
    }

    public static void main(String[] args) { new AppointmentDesk().setVisible(true); }
}
