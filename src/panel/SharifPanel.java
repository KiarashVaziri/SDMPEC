package panel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

class SharifPanel extends JPanel {
    Image SharifImage;
    JLabel SharifLabel;

    SharifPanel() {
        Color color = new Color(1, 1, 1, 1);
        setBounds(10, 595, 200, 70);
        setBackground(color);
        try {
            SharifImage = ImageIO.read(new File("icons/Sharif.png"));
            SharifLabel = new JLabel(new ImageIcon(SharifImage));
            add(SharifLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
