package panel;

import javax.swing.*;
import java.awt.*;

class WirePanel extends JPanel {
    int node1x, node1y, node2x, node2y;

    WirePanel(int node1, int node2) {
        node1x = ((node1 - 1) % 8) * 100 + 50;
        node1y = 550 - (node1 - 1) / 8 * 100;
        node2x = ((node2 - 1) % 8) * 100 + 50;
        node2y = 550 - (node2 - 1) / 8 * 100;
        setBounds((node1x + node2x) / 2 - 50, (node1y + node2y) / 2 - 50, 100, 100);
        Color color = new Color(1, 1, 1, 1);
        setBackground(color);
    }

    public void paint(Graphics g) {
        Graphics2D graphics = (Graphics2D) g;
        graphics.setStroke(new BasicStroke(2));
        graphics.setColor(Color.BLACK);
        if (node1x == node2x)
            graphics.drawLine(50, 0, 50, 100);
        else if (node1y == node2y)
            graphics.drawLine(0, 50, 100, 50);
    }
}
