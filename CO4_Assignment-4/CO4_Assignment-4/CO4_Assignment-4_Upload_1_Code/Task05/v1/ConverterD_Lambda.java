import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/** Style (d): lambda expression calling a private method.  Requirement v1. */
public class ConverterD_Lambda extends JFrame {
    private final JTextField amount = new JTextField(8);
    private final JComboBox<String> from = new JComboBox<>(Rates.CURRENCIES), to = new JComboBox<>(Rates.CURRENCIES);
    private final JButton convert = new JButton("Convert");
    private final JLabel result = new JLabel(" ");

    public ConverterD_Lambda() {
        super("Converter - (d) lambda");
        setLayout(new FlowLayout());
        to.setSelectedIndex(1);
        add(new JLabel("Amount")); add(amount); add(from); add(new JLabel("to")); add(to); add(convert); add(result);
        convert.addActionListener(e -> convert());
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { dispose(); } });
        pack();
    }
    private void convert() {
        try {
            double v = Double.parseDouble(amount.getText().trim());
            result.setText(String.format("= %.2f %s", Rates.convert((String) from.getSelectedItem(), (String) to.getSelectedItem(), v), to.getSelectedItem()));
        } catch (NumberFormatException ex) { result.setText("Invalid amount"); }
    }
    public static void main(String[] a) { SwingUtilities.invokeLater(() -> new ConverterD_Lambda().setVisible(true)); }
}
