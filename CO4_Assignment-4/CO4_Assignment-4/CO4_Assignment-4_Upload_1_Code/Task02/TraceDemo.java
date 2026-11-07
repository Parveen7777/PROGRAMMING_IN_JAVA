import java.awt.event.*;

/** Drives the desk programmatically and prints the real event order (evidence for Task 2.4). */
public class TraceDemo {
    public static void main(String[] args) throws Exception {
        AppointmentDesk d = new AppointmentDesk();
        d.setVisible(true);                                           // -> windowOpened, windowActivated
        Thread.sleep(600);
        d.dept.select(1);                                             // choose Orthopaedics
        d.dept.dispatchEvent(new ItemEvent(d.dept, ItemEvent.ITEM_STATE_CHANGED, d.dept.getSelectedItem(), ItemEvent.SELECTED));
        d.patientId.setText("P10234");
        d.book.dispatchEvent(new ActionEvent(d.book, ActionEvent.ACTION_PERFORMED, "Book Appointment"));
        Thread.sleep(300);
        d.dispatchEvent(new WindowEvent(d, WindowEvent.WINDOW_CLOSING));   // -> windowClosing -> dispose -> windowClosed
        Thread.sleep(500);
        System.exit(0);
    }
}
