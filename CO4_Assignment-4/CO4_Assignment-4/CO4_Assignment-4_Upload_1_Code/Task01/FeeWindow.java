import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** View + controller only: collects input and shows results. All rules live in FeeService. */
public class FeeWindow extends JFrame {
    private final JTextField regNo = new JTextField(14);
    private final JTextField amount = new JTextField(14);
    private final JLabel status = new JLabel(" ");
    private final FeeService service;
    private PaymentResult lastPayment;               // enables the Receipt button

    // one Action per button: no if-else chain, no string comparison
    private final Action payAction = new AbstractAction("Pay") {
        @Override public void actionPerformed(ActionEvent e) { startPayment(); }
    };
    private final Action clearAction = new AbstractAction("Clear") {
        @Override public void actionPerformed(ActionEvent e) { regNo.setText(""); amount.setText(""); show("", false); }
    };
    private final Action receiptAction = new AbstractAction("Receipt") {
        @Override public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(FeeWindow.this,
                "RECEIPT " + lastPayment.receiptNo() + "\n" + lastPayment.message(), "Receipt", JOptionPane.INFORMATION_MESSAGE);
        }
    };

    public FeeWindow(FeeService service) {
        super("Student Fee Payment");
        this.service = service;
        receiptAction.setEnabled(false);
        JPanel form = new JPanel(new GridLayout(2, 2, 6, 6));
        form.add(new JLabel("Register No:")); form.add(regNo);
        form.add(new JLabel("Amount (Rs):")); form.add(amount);
        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(new JButton(payAction)); buttons.add(new JButton(clearAction)); buttons.add(new JButton(receiptAction));
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        root.add(form, BorderLayout.NORTH); root.add(buttons, BorderLayout.CENTER); root.add(status, BorderLayout.SOUTH);
        setContentPane(root);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { dispose(); }
        });
        pack(); setLocationRelativeTo(null);
    }

    private void startPayment() {
        String err = service.validate(regNo.getText(), amount.getText());   // fast: runs on the EDT
        if (err != null) { show(err, true); return; }
        payAction.setEnabled(false);
        show("Contacting bank...", false);
        new SwingWorker<PaymentResult, Void>() {                              // slow: runs off the EDT
            @Override protected PaymentResult doInBackground() { return service.pay(regNo.getText(), amount.getText()); }
            @Override protected void done() {
                try {
                    lastPayment = get();
                    show(lastPayment.message(), !lastPayment.success());
                    receiptAction.setEnabled(lastPayment.success());
                } catch (Exception ex) { show("Unexpected error: " + ex.getMessage(), true); }
                payAction.setEnabled(true);
            }
        }.execute();
    }

    private void show(String msg, boolean error) {
        status.setForeground(error ? new Color(0xB00020) : new Color(0x1B5E20));
        status.setText(msg.isEmpty() ? " " : msg);
    }

    public static void main(String[] args) {
        FeeService svc = new FeeService(new InMemoryFeeRepository(), BankGateway.simulated(2000));
        // For MySQL: new JdbcFeeRepository("jdbc:mysql://localhost:3306/campus", "root", "password")
        SwingUtilities.invokeLater(() -> new FeeWindow(svc).setVisible(true));
    }
}
