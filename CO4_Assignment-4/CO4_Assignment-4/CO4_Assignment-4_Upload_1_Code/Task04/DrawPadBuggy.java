import java.awt.*;
import java.awt.event.*;

/** ORIGINAL DEFECTIVE PROGRAM (class renamed DrawPadBuggy so it can sit beside the fixed DrawPad). */
public class DrawPadBuggy extends Frame {
    int count = 0; Label status = new Label("Clicks: 0");
    public DrawPadBuggy() {
        add(status, BorderLayout.SOUTH);
        addMouseListener(new MouseAdapter() {
            public void mouseclicked(MouseEvent e) {          // DEFECT: lower-case 'c' -> a NEW method, not an override
                count++;
                Graphics g = getGraphics(); g.fillOval(e.getX(), e.getY(), 6, 6);
            }
        });
        setSize(400, 300); setVisible(true);
    }
    public static void main(String[] args) { new DrawPadBuggy(); }
}
