// ORIGINAL POOR DESIGN - kept only for the diagnosis in Task 1.1. It does NOT compile
// (URL/USER/PWD undefined, checked exceptions unhandled) - which is itself a deficiency.
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class FeeWindowBad extends Frame implements ActionListener {
    TextField regNo = new TextField(); TextField amount = new TextField();
    Button pay = new Button("Pay"), clear = new Button("Clear"), receipt = new Button("Receipt");
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() == "Pay") {                       // (2) == on Strings
            double a = Double.parseDouble(amount.getText());       // (3) no validation
            Connection c = DriverManager.getConnection(URL, USER, PWD);   // (4) DB on EDT, (9) leaked
            c.createStatement().executeUpdate("INSERT INTO fees VALUES('" + regNo.getText() + "'," + a + ")"); // (5) SQL injection
            Thread.sleep(3000);                                    // (4) freezes the window
            System.out.println("Paid");                            // (6) feedback only on console
        } else { regNo.setText(""); amount.setText(""); }          // (7) Receipt button clears the form!
    }
}
