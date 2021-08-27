package panel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

class DataPanel extends JComponent implements ActionListener {
    JButton load_button, save_button;
    JTextArea textArea;
    Border button_border, textArea_border;
    String filePath;
    JScrollPane scroll;
    Image TitleBoxImage;
    JLabel TitleBoxLabel, TitleLabel;

    DataPanel() {
        setBounds(25, 25, 225, 600);

        Color color = new Color(1, 1, 1, 1);
        try {
            TitleBoxImage = ImageIO.read(new File("icons/Data title box.png"));
            TitleBoxLabel = new JLabel(new ImageIcon(TitleBoxImage));
            TitleBoxLabel.setBounds(0, 0, 225, 30);
            TitleBoxLabel.setBackground(color);
            TitleLabel = new JLabel("Input");
            TitleLabel.setBounds(27, 7, 50, 20);
            TitleBoxLabel.add(TitleLabel);
            add(TitleBoxLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }

        button_border = BorderFactory.createLineBorder(Color.BLACK, 1);
        load_button = new JButton("     Load");
        load_button.addActionListener(this);
        int x_load_button = 10;
        int y_load_button = 530;
        int width_load_button = 100;
        int height_load_button = 30;
        load_button.setBounds(x_load_button, y_load_button, width_load_button, height_load_button);
        load_button.setBackground(Color.ORANGE);
        load_button.setBorder(button_border);
        Image Load_ButtonImage;
        JLabel Load_ButtonLabel;
        try {
            Load_ButtonImage = ImageIO.read(new File("icons/Load Button.png"));
            Load_ButtonLabel = new JLabel(new ImageIcon(Load_ButtonImage));
            load_button.add(Load_ButtonLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        add(load_button);

        save_button = new JButton("     Save");
        save_button.addActionListener(this);
        int x_run_button = x_load_button + width_load_button + 5;
        int y_run_button = 530;
        int width_run_button = 100;
        int height_run_button = 30;
        save_button.setBounds(x_run_button, y_run_button, width_run_button, height_run_button);
        save_button.setBackground(Color.ORANGE);
        save_button.setBorder(button_border);
        Image Save_ButtonImage;
        JLabel Save_ButtonLabel;
        try {
            Save_ButtonImage = ImageIO.read(new File("icons/Save Button.png"));
            Save_ButtonLabel = new JLabel(new ImageIcon(Save_ButtonImage));
            save_button.add(Save_ButtonLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        add(save_button);

        textArea = new JTextArea(10000, 300);
        textArea.setBounds(0, 30, 225, 470);
        textArea.setBackground(Color.WHITE);
        textArea_border = BorderFactory.createLineBorder(Color.black, 2);
        textArea.setBorder(textArea_border);
        scroll = new JScrollPane(textArea);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(scroll);
        add(textArea);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == load_button) {
            JFileChooser fc = new JFileChooser();
            int i = fc.showOpenDialog(this);
            if (i == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                filePath = f.getPath();
                try {
                    BufferedReader br = new BufferedReader(new FileReader(filePath));
                    String s1 = "", s2 = "";
                    while ((s1 = br.readLine()) != null) s2 += s1 + "\n";
                    textArea.setText(s2);
                    br.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            textArea.setFont(textArea.getFont().deriveFont(16f));
        }
        if (e.getSource() == save_button) {
            try {
                Writer w = new FileWriter(filePath);
                textArea.write(w);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
