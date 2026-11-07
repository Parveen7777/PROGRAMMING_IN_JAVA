import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/** Style (b): named inner class.  Requirement v2: button + Enter key, result in dialog. */
public class ConverterB_Inner extends JFrame {
    private final JTextField amount = new JTextField(8);
    private final JComboBox<String> from = new JComboBox<>(Rates.CURRENCIES), to = new JComboBox<>(Rates.CURRENCIES);
    private final JButton convert = new JButton("Convert");
    private final JLabel result = new JLabel(" ");

    private class ConvertHandler implements ActionListener {              // can use the outer fields directly
        @Override public void actionPerformed(ActionEvent e) {
            try {
                double v = Double.parseDouble(amount.getText().trim());
                JOptionPane.showMessageDialog(ConverterB_Inner.this, String.format("= %.2f %s", Rates.convert((String) from.getSelectedItem(), (String) to.getSelectedItem(), v), to.getSelectedItem()));
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(ConverterB_Inner.this, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE); }
        }
    }

    public ConverterB_Inner() {
        super("Converter - (b) named inner class");
        setLayout(new FlowLayout());
        to.setSelectedIndex(1);
        add(new JLabel("Amount")); add(amount); add(from); add(new JLabel("to")); add(to); add(convert); add(result);
        ConvertHandler h = new ConvertHandler();
        convert.addActionListener(h);
        amount.addActionListener(h);                          // Enter key
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { dispose(); } });
        pack();
    }
    public static void main(String[] a) { SwingUtilities.invokeLater(() -> new ConverterB_Inner().setVisible(true)); }
}
