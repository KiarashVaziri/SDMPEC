package panel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class GNDPanel extends JPanel {
    int nodex, nodey;

    GNDPanel(int node) {
        nodex = ((node - 1) % 8) * 100 + 50;
        nodey = 550 - (node - 1) / 8 * 100;
        setBounds(nodex, nodey - 5, 100, 100);
        Color color = new Color(1, 1, 1, 1);
        setBackground(color);
        BufferedImage GNDImage;
        JLabel GNDLabel, NameLabel;
        try {
            GNDImage = ImageIO.read(new File("icons/GND.png"));
            GNDLabel = new JLabel(new ImageIcon(GNDImage));
            NameLabel = new JLabel("GND");
            NameLabel.setBackground(color);
            NameLabel.setBounds(20, 50, 30, 10);
            GNDLabel.add(NameLabel);
            add(GNDLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
