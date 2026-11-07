import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/** Style (c): anonymous inner class.  Requirement v2: button + Enter key, result in dialog. */
public class ConverterC_Anonymous extends JFrame {
    private final JTextField amount = new JTextField(8);
    private final JComboBox<String> from = new JComboBox<>(Rates.CURRENCIES), to = new JComboBox<>(Rates.CURRENCIES);
    private final JButton convert = new JButton("Convert");
    private final JLabel result = new JLabel(" ");

    public ConverterC_Anonymous() {
        super("Converter - (c) anonymous inner class");
        setLayout(new FlowLayout());
        to.setSelectedIndex(1);
        add(new JLabel("Amount")); add(amount); add(from); add(new JLabel("to")); add(to); add(convert); add(result);
        ActionListener l = new ActionListener() {                         // must be stored in a variable to be shared
            @Override public void actionPerformed(ActionEvent e) {
                try {
                    double v = Double.parseDouble(amount.getText().trim());
                    JOptionPane.showMessageDialog(ConverterC_Anonymous.this, String.format("= %.2f %s", Rates.convert((String) from.getSelectedItem(), (String) to.getSelectedItem(), v), to.getSelectedItem()));
                } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(ConverterC_Anonymous.this, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE); }
            }
        };
        convert.addActionListener(l);
        amount.addActionListener(l);                                       // Enter key
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { dispose(); } });
        pack();
    }
    public static void main(String[] a) { SwingUtilities.invokeLater(() -> new ConverterC_Anonymous().setVisible(true)); }
}
