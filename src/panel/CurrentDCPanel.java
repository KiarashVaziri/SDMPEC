package panel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class CurrentDCPanel extends JPanel {
    int node1x, node1y, node2x, node2y;

    CurrentDCPanel(int node1, int node2, String name) {
        node1x = ((node1 - 1) % 8) * 100 + 50;
        node1y = 550 - (node1 - 1) / 8 * 100;
        node2x = ((node2 - 1) % 8) * 100 + 50;
        node2y = 550 - (node2 - 1) / 8 * 100;
        setBounds((node1x + node2x) / 2 - 50, (node1y + node2y) / 2 - 55, 100, 105);
        Color color = new Color(1, 1, 1, 1);
        setBackground(color);
        BufferedImage CurrentDCImage;
        JLabel CurrentDCLabel, NameLabel;
        if (node1x == node2x && node1y > node2y) {
            try {
                CurrentDCImage = ImageIO.read(new File("icons/Up side CurrentDC.png"));
                CurrentDCLabel = new JLabel(new ImageIcon(CurrentDCImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(20, 45, 50, 15);
                CurrentDCLabel.add(NameLabel);
                add(CurrentDCLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1x == node2x && node2y > node1y) {
            try {
                CurrentDCImage = ImageIO.read(new File("icons/Down side CurrentDC.png"));
                CurrentDCLabel = new JLabel(new ImageIcon(CurrentDCImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(20, 45, 50, 15);
                CurrentDCLabel.add(NameLabel);
                add(CurrentDCLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1y == node2y && node1x < node2x) {
            try {
                CurrentDCImage = ImageIO.read(new File("icons/Right side CurrentDC.png"));
                CurrentDCLabel = new JLabel(new ImageIcon(CurrentDCImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(45, 70, 50, 15);
                CurrentDCLabel.add(NameLabel);
                add(CurrentDCLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1y == node2y && node1x > node2x) {
            try {
                CurrentDCImage = ImageIO.read(new File("icons/Left side CurrentDC.png"));
                CurrentDCLabel = new JLabel(new ImageIcon(CurrentDCImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(45, 70, 50, 15);
                CurrentDCLabel.add(NameLabel);
                add(CurrentDCLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
