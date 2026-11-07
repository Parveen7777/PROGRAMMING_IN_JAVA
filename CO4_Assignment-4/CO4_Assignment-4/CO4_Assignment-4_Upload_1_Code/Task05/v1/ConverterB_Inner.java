import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/** Style (b): named inner class.  Requirement v1. */
public class ConverterB_Inner extends JFrame {
    private final JTextField amount = new JTextField(8);
    private final JComboBox<String> from = new JComboBox<>(Rates.CURRENCIES), to = new JComboBox<>(Rates.CURRENCIES);
    private final JButton convert = new JButton("Convert");
    private final JLabel result = new JLabel(" ");

    private class ConvertHandler implements ActionListener {              // can use the outer fields directly
        @Override public void actionPerformed(ActionEvent e) {
            try {
                double v = Double.parseDouble(amount.getText().trim());
                result.setText(String.format("= %.2f %s", Rates.convert((String) from.getSelectedItem(), (String) to.getSelectedItem(), v), to.getSelectedItem()));
            } catch (NumberFormatException ex) { result.setText("Invalid amount"); }
        }
    }

    public ConverterB_Inner() {
        super("Converter - (b) named inner class");
        setLayout(new FlowLayout());
        to.setSelectedIndex(1);
        add(new JLabel("Amount")); add(amount); add(from); add(new JLabel("to")); add(to); add(convert); add(result);
        convert.addActionListener(new ConvertHandler());
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { dispose(); } });
        pack();
    }
    public static void main(String[] a) { SwingUtilities.invokeLater(() -> new ConverterB_Inner().setVisible(true)); }
}
