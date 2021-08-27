package panel;

import circuit.Circuit;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;

class ResultPanel extends JComponent implements ActionListener {
    JButton run_button, draw_button, graph_button;
    JTextArea ResultArea;
    JScrollPane scrollPane;
    Circuit circuit;
    Circuit circuitToBeDrawn;
    CircuitFrame circuitFrame;
    DataPanel dataPanel;
    String outPutPath = "Output.txt";
    Image TitleBoxImage;
    JLabel TitleBoxLabel, TitleLabel;
    JFrame draw_alert;
    boolean runFlag = false;
    JFrame graph_alert;

    ResultPanel(DataPanel dp) {
        dataPanel = dp;
        setBounds(275, 25, 500, 600);
        Border button_border = BorderFactory.createLineBorder(Color.BLACK, 1);

        Color color = new Color(1, 1, 1, 1);
        try {
            TitleBoxImage = ImageIO.read(new File("icons/Result title box.png"));
            TitleBoxLabel = new JLabel(new ImageIcon(TitleBoxImage));
            TitleBoxLabel.setBounds(0, 0, 500, 30);
            TitleBoxLabel.setBackground(color);
            TitleLabel = new JLabel("Output");
            TitleLabel.setBounds(57, 7, 50, 20);
            TitleBoxLabel.add(TitleLabel);
            add(TitleBoxLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }

        run_button = new JButton("     Run");
        run_button.addActionListener(this);
        int x_load_button = 50;
        int y_load_button = 530;
        int width_load_button = 100;
        int height_load_button = 30;
        run_button.setBounds(x_load_button, y_load_button, width_load_button, height_load_button);
        run_button.setBackground(Color.ORANGE);
        run_button.setBorder(button_border);
        BufferedImage Run_ButtonImage;
        JLabel Run_ButtonLabel;
        try {
            Run_ButtonImage = ImageIO.read(new File("icons/Run Button.png"));
            Run_ButtonLabel = new JLabel(new ImageIcon(Run_ButtonImage));
            run_button.add(Run_ButtonLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        add(run_button);

        graph_button = new JButton("     Graph");
        graph_button.addActionListener(this);
        int x_graph_button = x_load_button + width_load_button + 50;
        int y_graph_button = 530;
        int width_graph_button = 100;
        int height_graph_button = 30;
        graph_button.setBounds(x_graph_button, y_graph_button, width_graph_button, height_graph_button);
        graph_button.setBackground(Color.ORANGE);
        graph_button.setBorder(button_border);
        Image Graph_ButtonImage;
        JLabel Graph_ButtonLabel;
        try {
            Graph_ButtonImage = ImageIO.read(new File("icons/Graph Button.png"));
            Graph_ButtonLabel = new JLabel(new ImageIcon(Graph_ButtonImage));
            graph_button.add(Graph_ButtonLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        add(graph_button);

        draw_button = new JButton("     Draw");
        draw_button.addActionListener(this);
        int x_run_button = x_graph_button + width_graph_button + 50;
        int y_run_button = 530;
        int width_run_button = 100;
        int height_run_button = 30;
        draw_button.setBounds(x_run_button, y_run_button, width_run_button, height_run_button);
        draw_button.setBackground(Color.ORANGE);
        draw_button.setBorder(button_border);
        Image Draw_ButtonImage;
        JLabel Draw_ButtonLabel;
        try {
            Draw_ButtonImage = ImageIO.read(new File("icons/Draw Button.png"));
            Draw_ButtonLabel = new JLabel(new ImageIcon(Draw_ButtonImage));
            draw_button.add(Draw_ButtonLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        add(draw_button);

        ResultArea = new JTextArea(8, 30);
        ResultArea.setEditable(false);
        Border ResultArea_border = BorderFactory.createLineBorder(Color.black, 1);
        ResultArea.setBorder(ResultArea_border);

        scrollPane = new JScrollPane(ResultArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBounds(0, 30, 500, 470);
        scrollPane.setBorder(ResultArea_border);

        add(scrollPane);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == run_button) {
            //Phase one
            if (dataPanel.filePath != null) {
                circuit = new Circuit(dataPanel.filePath);
                runFlag = true;
                try {
                    circuit.readFile();
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
                try {
                    circuit.updateCircuit();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                try {
                    ResultArea.setFont(ResultArea.getFont().deriveFont(15f));
                    BufferedReader br = new BufferedReader(new FileReader(outPutPath));
                    String s1 = "", s2 = "";
                    while ((s1 = br.readLine()) != null) s2 += s1 + "\n";
                    ResultArea.setText(s2);
                    br.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                draw_alert = new JFrame();
                JOptionPane.showMessageDialog(draw_alert, "No data to run, please try again.", "Alert", JOptionPane.WARNING_MESSAGE);
            }
        }
        if (e.getSource() == draw_button) {
            if (dataPanel.filePath != null) {
                circuitToBeDrawn = new Circuit(dataPanel.filePath);
                try {
                    circuitToBeDrawn.readFile();
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
                circuitFrame = new CircuitFrame(circuitToBeDrawn);
            } else {
                draw_alert = new JFrame();
                JOptionPane.showMessageDialog(draw_alert, "No data to draw, please try again.", "Alert", JOptionPane.WARNING_MESSAGE);
            }
        }
        if (e.getSource() == graph_button) {
            if (runFlag) {
                JFrame questionFrame = new JFrame("Input");
                String name = JOptionPane.showInputDialog(questionFrame, "Enter the name of your element");
                if (name != null) circuit.openCharts(name);
            } else {
                graph_alert = new JFrame();
                JOptionPane.showMessageDialog(graph_alert, "Please press run button first.", "Alert", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
