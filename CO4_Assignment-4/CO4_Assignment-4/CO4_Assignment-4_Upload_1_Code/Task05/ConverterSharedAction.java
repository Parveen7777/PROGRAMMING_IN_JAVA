import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/** Task 5 alternative: ONE Action object drives a button, a menu item, the Enter key and Ctrl+K. */
public class ConverterSharedAction extends JFrame {
    private final JTextField amount = new JTextField(8);
    private final JComboBox<String> from = new JComboBox<>(Rates.CURRENCIES), to = new JComboBox<>(Rates.CURRENCIES);
    private final JLabel result = new JLabel(" ");

    private final Action convertAction = new AbstractAction("Convert") {          // the single shared handler
        { putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK)); }
        @Override public void actionPerformed(ActionEvent e) {
            try {
                double v = Double.parseDouble(amount.getText().trim());
                result.setText(String.format("= %.2f %s", Rates.convert((String) from.getSelectedItem(), (String) to.getSelectedItem(), v), to.getSelectedItem()));
            } catch (NumberFormatException ex) { result.setText("Invalid amount"); }
        }
    };

    public ConverterSharedAction() {
        super("Converter - shared Action");
        setLayout(new FlowLayout());
        to.setSelectedIndex(1);
        add(new JLabel("Amount")); add(amount); add(from); add(new JLabel("to")); add(to);
        add(new JButton(convertAction));                                           // trigger 1: button
        add(result);
        amount.addActionListener(convertAction);                                   // trigger 2: Enter key
        JMenuBar bar = new JMenuBar(); JMenu m = new JMenu("Actions");
        m.add(new JMenuItem(convertAction)); bar.add(m); setJMenuBar(bar);          // trigger 3: menu item + Ctrl+K
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
    }
    public static void main(String[] a) { SwingUtilities.invokeLater(() -> new ConverterSharedAction().setVisible(true)); }
}
