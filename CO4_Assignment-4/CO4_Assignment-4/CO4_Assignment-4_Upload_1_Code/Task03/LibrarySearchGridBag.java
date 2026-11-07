import java.awt.*;
import java.awt.event.*;

/** Task 3 alternative: ONE container + GridBagLayout instead of nested Border/Flow panels (layout only, no logic). */
public class LibrarySearchGridBag extends Frame {
    public LibrarySearchGridBag() {
        super("Library - GridBagLayout version");
        setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4); g.fill = GridBagConstraints.HORIZONTAL;
        Label title = new Label("CAMPUS LIBRARY CATALOGUE", Label.CENTER);
        title.setBackground(new Color(0x1F3A5F)); title.setForeground(Color.WHITE);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 4; g.weightx = 1; add(title, g);
        g.gridwidth = 1; g.gridy = 1; g.weightx = 0; add(new Label("Search:"), g);
        g.gridx = 1; g.weightx = 1; add(new TextField(20), g);
        Choice c = new Choice(); c.add("All"); c.add("Physics");
        g.gridx = 2; g.weightx = 0; add(c, g);
        g.gridx = 3; add(new Button("Search"), g);
        g.gridx = 0; g.gridy = 2; g.gridwidth = 4; g.weightx = 1; g.weighty = 1; g.fill = GridBagConstraints.BOTH; add(new java.awt.List(10), g);
        g.gridy = 3; g.weighty = 0; g.fill = GridBagConstraints.NONE; Panel p = new Panel();
        p.add(new Button("Prev")); p.add(new Button("Next")); p.add(new Button("Reserve")); add(p, g);
        g.gridy = 4; g.fill = GridBagConstraints.HORIZONTAL; add(new Label("Status"), g);
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { dispose(); } });
        setSize(640, 420);
    }
    public static void main(String[] a) { new LibrarySearchGridBag().setVisible(true); }
}
