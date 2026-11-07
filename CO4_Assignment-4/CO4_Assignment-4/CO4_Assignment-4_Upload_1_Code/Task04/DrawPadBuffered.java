import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/** Task 4 alternative: dots are drawn ONCE onto an off-screen image; paint() just copies the image. */
public class DrawPadBuffered extends Frame {
    private BufferedImage canvas = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
    private int count = 0;
    private final Label status = new Label("Clicks: 0");

    public DrawPadBuffered() {
        super("Draw Pad (BufferedImage)");
        add(status, BorderLayout.SOUTH);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                ensureSize();
                Graphics2D g2 = canvas.createGraphics();
                g2.fillOval(e.getX() - 3, e.getY() - 3, 6, 6);         // O(1) per click
                g2.dispose();
                status.setText("Clicks: " + (++count));
                repaint();
            }
        });
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { dispose(); } });
        setSize(400, 300);
    }
    private void ensureSize() {                                         // grow the image if the window was enlarged
        if (getWidth() > canvas.getWidth() || getHeight() > canvas.getHeight()) {
            BufferedImage n = new BufferedImage(Math.max(getWidth(), canvas.getWidth()), Math.max(getHeight(), canvas.getHeight()), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = n.createGraphics(); g2.drawImage(canvas, 0, 0, null); g2.dispose(); canvas = n;
        }
    }
    @Override public void paint(Graphics g) { super.paint(g); g.drawImage(canvas, 0, 0, null); }   // one copy, independent of n
    public static void main(String[] args) { new DrawPadBuffered().setVisible(true); }
}
