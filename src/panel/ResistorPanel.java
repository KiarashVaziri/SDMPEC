package panel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class ResistorPanel extends JPanel {
    int node1x, node1y, node2x, node2y;

    ResistorPanel(int node1, int node2, String name) {
        node1x = ((node1 - 1) % 8) * 100 + 50;
        node1y = 550 - (node1 - 1) / 8 * 100;
        node2x = ((node2 - 1) % 8) * 100 + 50;
        node2y = 550 - (node2 - 1) / 8 * 100;
        setBounds((node1x + node2x) / 2 - 50, (node1y + node2y) / 2 - 55, 100, 105);
        Color color = new Color(1, 1, 1, 1);
        setBackground(color);
        BufferedImage ResistorImage;
        JLabel ResistorLabel, NameLabel;
        if (node1x == node2x) {
            try {
                ResistorImage = ImageIO.read(new File("icons/Vertical Resistor.png"));
                ResistorLabel = new JLabel(new ImageIcon(ResistorImage));
                NameLabel = new JLabel(name);
                NameLabel.setBounds(15, 45, 50, 15);
                NameLabel.setBackground(color);
                ResistorLabel.add(NameLabel);
                add(ResistorLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1y == node2y) {
            try {
                ResistorImage = ImageIO.read(new File("icons/Horizontal Resistor.png"));
                ResistorLabel = new JLabel(new ImageIcon(ResistorImage));
                NameLabel = new JLabel(name);
                NameLabel.setBounds(45, 60, 50, 15);
                NameLabel.setBackground(color);
                ResistorLabel.add(NameLabel);
                add(ResistorLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
