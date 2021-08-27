package panel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class InductorPanel extends JPanel {
    int node1x, node1y, node2x, node2y;

    InductorPanel(int node1, int node2, String name) {
        node1x = ((node1 - 1) % 8) * 100 + 50;
        node1y = 550 - (node1 - 1) / 8 * 100;
        node2x = ((node2 - 1) % 8) * 100 + 50;
        node2y = 550 - (node2 - 1) / 8 * 100;
        setBounds((node1x + node2x) / 2 - 50, (node1y + node2y) / 2 - 55, 100, 105);
        Color color = new Color(1, 1, 1, 1);
        setBackground(color);
        BufferedImage InductorImage;
        JLabel InductorLabel, NameLabel;
        if (node1x == node2x) {
            try {
                InductorImage = ImageIO.read(new File("icons/Vertical Inductor.png"));
                InductorLabel = new JLabel(new ImageIcon(InductorImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(20, 40, 50, 15);
                InductorLabel.add(NameLabel);
                add(InductorLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1y == node2y) {
            try {
                InductorImage = ImageIO.read(new File("icons/Horizontal Inductor.png"));
                InductorLabel = new JLabel(new ImageIcon(InductorImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(40, 60, 50, 15);
                InductorLabel.add(NameLabel);
                add(InductorLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
