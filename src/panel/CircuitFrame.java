package panel;

import circuit.Circuit;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.File;
import java.io.IOException;

class CircuitFrame extends JFrame {
    int startNode, endNode;
    int locationMatrix[][] = new int[15][11];
    JPanel panel;
    Border border;
    Image TitleBoxImage;
    JLabel TitleBoxLabel, TitleLabel;

    CircuitFrame(Circuit circuit) {
        setSize(970, 820);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container content = this.getContentPane();
        content.setBackground(Color.DARK_GRAY);
        JRootPane root;
        root = this.getRootPane();
        panel = new JPanel();
        panel.setBounds(25, 50, 900, 700);
        panel.setBackground(Color.WHITE);
        panel.setLayout(null);
        border = BorderFactory.createLineBorder(Color.BLACK, 1);
        panel.setBorder(border);
        Color color = new Color(1, 1, 1, 1);
        try {
            TitleBoxImage = ImageIO.read(new File("icons/circuit.Circuit title box.png"));
            TitleBoxLabel = new JLabel(new ImageIcon(TitleBoxImage));
            TitleBoxLabel.setBounds(25, 20, 900, 30);
            TitleBoxLabel.setBackground(color);
            TitleLabel = new JLabel("circuit.Circuit");
            TitleLabel.setBounds(57, 7, 50, 20);
            TitleBoxLabel.add(TitleLabel);
            add(TitleBoxLabel);
        } catch (IOException e) {
            System.out.println("not up to date yet!");
        }
        LayoutManager mgr;
        mgr = new GroupLayout(content);
        this.setLayout(mgr);
        Border border = BorderFactory.createLineBorder(Color.BLACK, 2);
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 11; j++)
                locationMatrix[i][j] = 0;
        }
        for (int i = 0; i < circuit.numberOfBranches; i++) {
            startNode = -1;
            endNode = -1;
            for (int j = 1; j <= circuit.numberOfNodes; j++) {
                if (circuit.adjMatrix[i][j] == 1)
                    startNode = j;
                else if (circuit.adjMatrix[i][j] == -1)
                    endNode = j;
            }
            if (startNode != -1 && endNode != -1) {
                startNode = startNode + ((startNode - 1) / 6) * 2 + 9;
                endNode = endNode + ((endNode - 1) / 6) * 2 + 9;
                new Element(panel, locationMatrix, circuit.branchArray[i].name.charAt(0), circuit.branchArray[i], startNode, endNode);
            }
        }
        for (int i = 0; i < circuit.numberOfBranches; i++) {
            startNode = 0;
            endNode = 0;
            for (int j = 1; j <= circuit.numberOfNodes; j++) {
                if (circuit.adjMatrix[i][j] == 1)
                    startNode = j;
                else if (circuit.adjMatrix[i][j] == -1)
                    endNode = j;
            }
            if ((circuit.adjMatrix[i][0] == 1 && endNode != 0) || (circuit.adjMatrix[i][0] == -1 && startNode != 0)) {
                if (startNode != 0)
                    startNode = startNode + ((startNode - 1) / 6) * 2 + 9;
                if (endNode != 0)
                    endNode = endNode + ((endNode - 1) / 6) * 2 + 9;
                new Element(panel, locationMatrix, circuit.branchArray[i].name.charAt(0), circuit.branchArray[i], startNode, endNode);
            }
        }
        add(panel);
        setVisible(true);
    }
}
