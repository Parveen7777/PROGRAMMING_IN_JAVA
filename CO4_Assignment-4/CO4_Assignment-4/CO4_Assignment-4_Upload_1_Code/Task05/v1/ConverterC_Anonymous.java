import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/** Style (c): anonymous inner class.  Requirement v1. */
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
        convert.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                try {
                    double v = Double.parseDouble(amount.getText().trim());
                    result.setText(String.format("= %.2f %s", Rates.convert((String) from.getSelectedItem(), (String) to.getSelectedItem(), v), to.getSelectedItem()));
                } catch (NumberFormatException ex) { result.setText("Invalid amount"); }
            }
        });
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { dispose(); } });
        pack();
    }
    public static void main(String[] a) { SwingUtilities.invokeLater(() -> new ConverterC_Anonymous().setVisible(true)); }
}
