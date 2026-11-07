import java.awt.*;
import java.awt.event.*;

/** Task 4.2 demo: the method name is fixed, but dots are still drawn with getGraphics() instead of paint(). */
public class DrawPadHalfFixed extends Frame {
    int count = 0; Label status = new Label("Clicks: 0");
    public DrawPadHalfFixed() {
        super("Half-fixed (getGraphics)");
        add(status, BorderLayout.SOUTH);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                status.setText("Clicks: " + (++count));
                Graphics g = getGraphics(); g.fillOval(e.getX() - 3, e.getY() - 3, 6, 6);   // one-off drawing: not remembered
                g.dispose();
            }
        });
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { dispose(); } });
        setSize(400, 300);
    }
    public static void main(String[] a) { new DrawPadHalfFixed().setVisible(true); }
}
