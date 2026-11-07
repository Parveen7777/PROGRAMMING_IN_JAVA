import java.awt.*;
import java.awt.event.*;

/** Task 4.4 - THIS FILE IS MEANT TO FAIL TO COMPILE. @Override exposes the misspelt method name. */
public class DrawPadTypo extends Frame {
    public DrawPadTypo() {
        addMouseListener(new MouseAdapter() {
            @Override public void mouseclicked(MouseEvent e) { }   // misspelt on purpose
        });
    }
}
