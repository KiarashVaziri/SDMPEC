//import Elements.CurrentDependentCS;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Ground extends Node {
    static final double voltage = 0;
    Node[] node_array;

    Ground(int a) {
        super(a);
    }
}

class ChartDrawer extends Canvas {
    int type, size;
    ArrayList<Float> list;
    float duration;
    float max = 0, min = 0, scale;
    String order;
    String orderOfTime;

    /* We have 3 types of outputs:
       1.voltage
       2.current
       3.power  */

    ChartDrawer(int type, ArrayList<Float> data, float duration) {
        this.type = type;
        list = data;
        this.duration = duration;
        for (int i = 0; i < list.size(); i++) {
            if (max < list.get(i)) max = list.get(i);
            if (min > list.get(i)) min = list.get(i);
        }
        size = list.size();
        if (Math.abs(max) > Math.abs(min)) scale = max;
        else scale = Math.abs(min);

        if (scale >= 1000) order = "k";
        else if (scale < 1000 && scale > 0.1f) order = "";
        else if (scale < 0.1f) order = "m";

        if (duration >= 0.1f) orderOfTime = "";
        else if (duration < 0.1f && duration >= 1E-4) orderOfTime = "m";
        else if (duration < 1E-4) orderOfTime = "u";
    }

    public void paint(Graphics graphics) {
        setBackground(Color.black);
        int startX = 60, startY = 340, width, height;

        //Y-axis
        for (int i = 0; i <= 800; i += 80) {
            Float value = 0f;
            if (orderOfTime.equals("")) value = duration * (float) i / 800;
            else if (orderOfTime.equals("m")) value = 1000 * duration * (float) i / 800;
            else if (orderOfTime.equals("u")) value = 1000000 * duration * (float) i / 800;

            //String str = value.toString();
            String string = String.format("%.2f", value);
            graphics.setColor(Color.gray);
            graphics.drawString(string, startX + i - 10, startY + 320);
            graphics.setColor(Color.darkGray);
            graphics.drawLine(startX + i, startY - 300, startX + i, startY + 300);
        }
        graphics.setColor(Color.WHITE);
        if (type == 1) graphics.drawString("voltage [ " + order + "V ]", startX, startY - 310);
        else if (type == 2) graphics.drawString("current [ " + order + "A ]", startX, startY - 310);
        else if (type == 3) graphics.drawString("power [ " + order + "W ]", startX, startY - 310);
        else if (type == 4) graphics.drawString("voltage [ " + order + "V ]", startX, startY - 310);
        graphics.drawLine(startX, startY - 300, startX, startY + 300);

        //X-axis
        for (int i = -300; i <= 300; i += 60) {
            Float value = 0f;
            if (order.equals("k")) value = scale * (float) i / 300 / 1000;
            else if (order.equals("")) value = scale * (float) i / 300;
            else if (order.equals("m")) value = scale * 1000 * (float) i / 300;
            //String str = value.toString();
            String string = String.format("%.2f", value);
            graphics.setColor(Color.gray);
            graphics.drawString(string, startX - 40, startY - i);
            graphics.setColor(Color.darkGray);
            graphics.drawLine(startX, startY - i, startX + 800, startY - i);
        }
        graphics.setColor(Color.WHITE);
        graphics.drawString("time [" + orderOfTime + "s]", startX + 810, startY);
        graphics.drawLine(startX, startY, startX + 800, startY);

        //draw data
        float stepX = 800 / (float) size;
        float stepY = -300 / scale;
        float xp = startX;
        float yp = stepY * list.get(0) + startY;

        if (type == 1) graphics.setColor(Color.green);
        if (type == 2) graphics.setColor(Color.ORANGE);
        if (type == 3) graphics.setColor(Color.RED);
        if (type == 4) graphics.setColor(Color.magenta);
        for (int i = 1; i < size; i++) {
            float y = stepY * list.get(i) + startY;
            graphics.drawLine((int) xp, (int) yp, (int) (xp + stepX), (int) y);
            yp = y;
            xp += stepX;
        }
    }
}

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

class CapacitorPanel extends JPanel {
    int node1x, node1y, node2x, node2y;

    CapacitorPanel(int node1, int node2, String name) {
        node1x = ((node1 - 1) % 8) * 100 + 50;
        node1y = 550 - (node1 - 1) / 8 * 100;
        node2x = ((node2 - 1) % 8) * 100 + 50;
        node2y = 550 - (node2 - 1) / 8 * 100;
        setBounds((node1x + node2x) / 2 - 50, (node1y + node2y) / 2 - 55, 100, 105);
        Color color = new Color(1, 1, 1, 1);
        setBackground(color);
        BufferedImage CapacitorImage;
        JLabel CapacitorLabel, NameLabel;
        if (node1x == node2x) {
            try {
                CapacitorImage = ImageIO.read(new File("icons/Vertical Capacitor.png"));
                CapacitorLabel = new JLabel(new ImageIcon(CapacitorImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(15, 30, 50, 15);
                CapacitorLabel.add(NameLabel);
                add(CapacitorLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1y == node2y) {
            try {
                CapacitorImage = ImageIO.read(new File("icons/Horizontal Capacitor.png"));
                CapacitorLabel = new JLabel(new ImageIcon(CapacitorImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(20, 60, 50, 15);
                CapacitorLabel.add(NameLabel);
                add(CapacitorLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

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

class VoltageDCPanel extends JPanel {
    int node1x, node1y, node2x, node2y;

    VoltageDCPanel(int node1, int node2, String name) {
        node1x = ((node1 - 1) % 8) * 100 + 50;
        node1y = 550 - (node1 - 1) / 8 * 100;
        node2x = ((node2 - 1) % 8) * 100 + 50;
        node2y = 550 - (node2 - 1) / 8 * 100;
        setBounds((node1x + node2x) / 2 - 50, (node1y + node2y) / 2 - 55, 100, 105);
        Color color = new Color(1, 1, 1, 1);
        setBackground(color);
        BufferedImage VoltageDCImage;
        JLabel VoltageDCLabel, NameLabel;
        if (node1x == node2x && node1y < node2y) {
            try {
                VoltageDCImage = ImageIO.read(new File("icons/Up side VoltageDC.png"));
                VoltageDCLabel = new JLabel(new ImageIcon(VoltageDCImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(15, 45, 50, 15);
                VoltageDCLabel.add(NameLabel);
                add(VoltageDCLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1x == node2x && node2y < node1y) {
            try {
                VoltageDCImage = ImageIO.read(new File("icons/Down side VoltageDC.png"));
                VoltageDCLabel = new JLabel(new ImageIcon(VoltageDCImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(15, 45, 50, 15);
                VoltageDCLabel.add(NameLabel);
                add(VoltageDCLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1y == node2y && node1x > node2x) {
            try {
                VoltageDCImage = ImageIO.read(new File("icons/Right side VoltageDC.png"));
                VoltageDCLabel = new JLabel(new ImageIcon(VoltageDCImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(45, 65, 50, 15);
                VoltageDCLabel.add(NameLabel);
                add(VoltageDCLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1y == node2y && node1x < node2x) {
            try {
                VoltageDCImage = ImageIO.read(new File("icons/Left side VoltageDC.png"));
                VoltageDCLabel = new JLabel(new ImageIcon(VoltageDCImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(45, 65, 50, 15);
                VoltageDCLabel.add(NameLabel);
                add(VoltageDCLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

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

class DependentCurrentPanel extends JPanel {
    int node1x, node1y, node2x, node2y;

    DependentCurrentPanel(int node1, int node2, String name) {
        node1x = ((node1 - 1) % 8) * 100 + 50;
        node1y = 550 - (node1 - 1) / 8 * 100;
        node2x = ((node2 - 1) % 8) * 100 + 50;
        node2y = 550 - (node2 - 1) / 8 * 100;
        setBounds((node1x + node2x) / 2 - 50, (node1y + node2y) / 2 - 55, 100, 105);
        Color color = new Color(1, 1, 1, 1);
        setBackground(color);
        BufferedImage DependentCurrentImage;
        JLabel DependentCurrentLabel, NameLabel;
        if (node1x == node2x && node1y > node2y) {
            try {
                DependentCurrentImage = ImageIO.read(new File("icons/Up side DependentCurrent.png"));
                DependentCurrentLabel = new JLabel(new ImageIcon(DependentCurrentImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(20, 60, 50, 15);
                DependentCurrentLabel.add(NameLabel);
                add(DependentCurrentLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1x == node2x && node2y > node1y) {
            try {
                DependentCurrentImage = ImageIO.read(new File("icons/Down side DependentCurrent.png"));
                DependentCurrentLabel = new JLabel(new ImageIcon(DependentCurrentImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(20, 60, 50, 15);
                DependentCurrentLabel.add(NameLabel);
                add(DependentCurrentLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1y == node2y && node1x > node2x) {
            try {
                DependentCurrentImage = ImageIO.read(new File("icons/Right side DependentCurrent.png"));
                DependentCurrentLabel = new JLabel(new ImageIcon(DependentCurrentImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(60, 60, 50, 15);
                DependentCurrentLabel.add(NameLabel);
                add(DependentCurrentLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1y == node2y && node1x < node2x) {
            try {
                DependentCurrentImage = ImageIO.read(new File("icons/Left side DependentCurrent.png"));
                DependentCurrentLabel = new JLabel(new ImageIcon(DependentCurrentImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(60, 60, 50, 15);
                DependentCurrentLabel.add(NameLabel);
                add(DependentCurrentLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class DependentVoltagePanel extends JPanel {
    int node1x, node1y, node2x, node2y;

    DependentVoltagePanel(int node1, int node2, String name) {
        node1x = ((node1 - 1) % 8) * 100 + 50;
        node1y = 550 - (node1 - 1) / 8 * 100;
        node2x = ((node2 - 1) % 8) * 100 + 50;
        node2y = 550 - (node2 - 1) / 8 * 100;
        setBounds((node1x + node2x) / 2 - 50, (node1y + node2y) / 2 - 55, 100, 105);
        Color color = new Color(1, 1, 1, 1);
        setBackground(color);
        BufferedImage DependentVoltageImage;
        JLabel DependentVoltageLabel, NameLabel;
        if (node1x == node2x && node1y > node2y) {
            try {
                DependentVoltageImage = ImageIO.read(new File("icons/Down side DependentVoltage.png"));
                DependentVoltageLabel = new JLabel(new ImageIcon(DependentVoltageImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(25, 60, 50, 15);
                DependentVoltageLabel.add(NameLabel);
                add(DependentVoltageLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1x == node2x && node2y > node1y) {
            try {
                DependentVoltageImage = ImageIO.read(new File("icons/Up side DependentVoltage.png"));
                DependentVoltageLabel = new JLabel(new ImageIcon(DependentVoltageImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(25, 60, 50, 15);
                DependentVoltageLabel.add(NameLabel);
                add(DependentVoltageLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1y == node2y && node1x < node2x) {
            try {
                DependentVoltageImage = ImageIO.read(new File("icons/Left side DependentVoltage.png"));
                DependentVoltageLabel = new JLabel(new ImageIcon(DependentVoltageImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(60, 60, 50, 15);
                DependentVoltageLabel.add(NameLabel);
                add(DependentVoltageLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1y == node2y && node1x > node2x) {
            try {
                DependentVoltageImage = ImageIO.read(new File("icons/Right side DependentVoltage.png"));
                DependentVoltageLabel = new JLabel(new ImageIcon(DependentVoltageImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(60, 60, 50, 15);
                DependentVoltageLabel.add(NameLabel);
                add(DependentVoltageLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class DiodePanel extends JPanel {
    int node1x, node1y, node2x, node2y;

    DiodePanel(int node1, int node2, String name) {
        node1x = ((node1 - 1) % 8) * 100 + 50;
        node1y = 550 - (node1 - 1) / 8 * 100;
        node2x = ((node2 - 1) % 8) * 100 + 50;
        node2y = 550 - (node2 - 1) / 8 * 100;
        setBounds((node1x + node2x) / 2 - 50, (node1y + node2y) / 2 - 55, 100, 105);
        Color color = new Color(1, 1, 1, 1);
        setBackground(color);
        BufferedImage DiodeImage;
        JLabel DiodeLabel, NameLabel;
        if (node1x == node2x && node1y > node2y) {
            try {
                DiodeImage = ImageIO.read(new File("icons/Up side Diode.png"));
                DiodeLabel = new JLabel(new ImageIcon(DiodeImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(15, 45, 50, 15);
                DiodeLabel.add(NameLabel);
                add(DiodeLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1x == node2x && node2y > node1y) {
            try {
                DiodeImage = ImageIO.read(new File("icons/Down side Diode.png"));
                DiodeLabel = new JLabel(new ImageIcon(DiodeImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(15, 45, 50, 15);
                DiodeLabel.add(NameLabel);
                add(DiodeLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1y == node2y && node1x < node2x) {
            try {
                DiodeImage = ImageIO.read(new File("icons/Right side Diode.png"));
                DiodeLabel = new JLabel(new ImageIcon(DiodeImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(45, 65, 50, 15);
                DiodeLabel.add(NameLabel);
                add(DiodeLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1y == node2y && node1x > node2x) {
            try {
                DiodeImage = ImageIO.read(new File("icons/Left side Diode.png"));
                DiodeLabel = new JLabel(new ImageIcon(DiodeImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(45, 65, 50, 15);
                DiodeLabel.add(NameLabel);
                add(DiodeLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

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

class Element {
    int dx, dy, n1i, n1j, n2i, n2j;

    Element(JPanel frame, int locationMatrix[][], char type, Branch branch, int node1, int node2) {
        dx = (node2 - 1) % 8 - (node1 - 1) % 8;
        dy = (node1 - 1) / 8 - (node2 - 1) / 8;
        n1i = ((node1 - 1) % 8) * 2;
        n1j = (5 - (node1 - 1) / 8) * 2;
        n2i = ((node2 - 1) % 8) * 2;
        n2j = (5 - (node2 - 1) / 8) * 2;
        Draw(frame, locationMatrix, type, branch, node1, node2, dx, dy);
    }

    void Draw(JPanel frame, int locationMatrix[][], char type, Branch branch, int node1, int node2, int dx, int dy) {
        n1i = ((node1 - 1) % 8) * 2;
        n1j = (5 - (node1 - 1) / 8) * 2;
        n2i = ((node2 - 1) % 8) * 2;
        n2j = (5 - (node2 - 1) / 8) * 2;
        if (node1 == 0) {
            if (n2i + 1 <= 11 && locationMatrix[n2i + 2][n2j] == 0)
                node1 = node2 + 1;
            else if (n2i - 1 >= 2 && locationMatrix[n2i - 2][n2j] == 0)
                node1 = node2 - 1;
            else if (n2j + 1 <= 9 && locationMatrix[n2i][n2j + 2] == 0)
                node1 = node2 - 8;
            else if (n2j - 1 >= 0 && locationMatrix[n2i][n2j - 2] == 0)
                node1 = node2 + 8;
            n1i = ((node1 - 1) % 8) * 2;
            n1j = (5 - (node1 - 1) / 8) * 2;
            dx = (node2 - 1) % 8 - (node1 - 1) % 8;
            dy = (node1 - 1) / 8 - (node2 - 1) / 8;
            GNDPanel g = new GNDPanel(node1);
            frame.add(g);
            Draw(frame, locationMatrix, type, branch, node1, node2, dx, dy);
        } else if (node2 == 0) {
            if (n1i + 1 <= 9 && locationMatrix[n1i + 2][n1j] == 0)
                node2 = node1 + 1;
            else if (n1i - 1 >= 0 && locationMatrix[n1i - 2][n1j] == 0)
                node2 = node1 - 1;
            else if (n1j + 1 <= 9 && locationMatrix[n1i][n1j + 2] == 0)
                node2 = node1 - 8;
            else if (n1j - 1 >= 0 && locationMatrix[n1i][n1j - 2] == 0)
                node2 = node1 + 8;
            n2i = ((node2 - 1) % 8) * 2;
            n2j = (5 - (node2 - 1) / 8) * 2;
            dx = (node2 - 1) % 8 - (node1 - 1) % 8;
            dy = (node1 - 1) / 8 - (node2 - 1) / 8;
            GNDPanel g = new GNDPanel(node2);
            frame.add(g);
            Draw(frame, locationMatrix, type, branch, node1, node2, dx, dy);
        } else if ((Math.abs(n1i - n2i) == 2 && n1j == n2j && locationMatrix[(n1i + n2i) / 2][n1j] == 0) || (Math.abs(n1j - n2j) == 2 && n1i == n2i && locationMatrix[n1i][(n1j + n2j) / 2] == 0)) {
            locationMatrix[n1i][n1j] = 1;
            locationMatrix[n2i][n2j] = 1;
            locationMatrix[(n1i + n2i) / 2][(n1j + n2j) / 2] = 1;
            if (type == 'R') {
                ResistorPanel r = new ResistorPanel(node1, node2, branch.name);
                frame.add(r);
            } else if (type == 'C') {
                CapacitorPanel c = new CapacitorPanel(node1, node2, branch.name);
                frame.add(c);
            } else if (type == 'L') {
                InductorPanel l = new InductorPanel(node1, node2, branch.name);
                frame.add(l);
            } else if (type == 'V') {
                VoltageDCPanel v = new VoltageDCPanel(node1, node2, branch.name);
                frame.add(v);
            } else if (type == 'I') {
                CurrentDCPanel i = new CurrentDCPanel(node1, node2, branch.name);
                frame.add(i);
            } else if (type == 'H') {
                DependentVoltagePanel h = new DependentVoltagePanel(node1, node2, branch.name);
                frame.add(h);
            } else if (type == 'G') {
                DependentCurrentPanel e = new DependentCurrentPanel(node1, node2, branch.name);
                frame.add(e);
            } else if (type == 'E') {
                DependentVoltagePanel g = new DependentVoltagePanel(node1, node2, branch.name);
                frame.add(g);
            } else if (type == 'F') {
                DependentCurrentPanel f = new DependentCurrentPanel(node1, node2, branch.name);
                frame.add(f);
            } else if (type == 'D') {
                DiodePanel d = new DiodePanel(node1, node2, branch.name);
                frame.add(d);
            }
        } else {
            if (dx == 0) {
                if (dy != 0 && locationMatrix[n1i][n1j + (dy / Math.abs(dy))] == 0 && locationMatrix[n1i][n1j + 2 * (dy / Math.abs(dy))] == 0) {
                    WirePanel w = new WirePanel(node1, node1 - 8 * dy / Math.abs(dy));
                    frame.add(w);
                    locationMatrix[n1i][n1j + (dy / Math.abs(dy))] = 1;
                    locationMatrix[n1i][n1j + 2 * (dy / Math.abs(dy))] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 - 8 * dy / Math.abs(dy), node2, dx, (dy / Math.abs(dy)) * (Math.abs(dy) - 1));
                } else if (n1i + 1 <= 13 && locationMatrix[n1i + 1][n1j] == 0 && locationMatrix[n1i + 2][n1j] == 0) {
                    WirePanel w = new WirePanel(node1, node1 + 1);
                    frame.add(w);
                    locationMatrix[n1i + 1][n1j] = 1;
                    locationMatrix[n1i + 2][n1j] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 + 1, node2, dx - 1, dy);
                } else if (n1i - 1 >= 0 && locationMatrix[n1i - 1][n1j] == 0 && locationMatrix[n1i - 2][n1j] == 0) {
                    WirePanel w = new WirePanel(node1, node1 - 1);
                    frame.add(w);
                    locationMatrix[n1i - 1][n1j] = 1;
                    locationMatrix[n1i - 2][n1j] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 - 1, node2, dx + 1, dy);
                }
            } else if (dy == 0) {
                if (dx != 0 && locationMatrix[n1i + (dx / Math.abs(dx))][n1j] == 0 && locationMatrix[n1i + 2 * (dx / Math.abs(dx))][n1j] == 0) {
                    WirePanel w = new WirePanel(node1, node1 + dx / Math.abs(dx));
                    frame.add(w);
                    locationMatrix[n1i + (dx / Math.abs(dx))][n1j] = 1;
                    locationMatrix[n1i + 2 * (dx / Math.abs(dx))][n1j] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 + dx / Math.abs(dx), node2, (dx / Math.abs(dx)) * (Math.abs(dx) - 1), dy);
                } else if (n1j + 1 <= 9 && locationMatrix[n1i][n1j + 1] == 0 && locationMatrix[n1i][n1j + 2] == 0) {
                    WirePanel w = new WirePanel(node1, node1 - 8);
                    frame.add(w);
                    locationMatrix[n1i][n1j + 1] = 1;
                    locationMatrix[n1i][n1j + 2] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 - 8, node2, dx, dy - 1);
                } else if (n1j - 1 >= 0 && locationMatrix[n1i][n1j - 1] == 0 && locationMatrix[n1i][n1j - 2] == 0) {
                    WirePanel w = new WirePanel(node1, node1 + 8);
                    frame.add(w);
                    locationMatrix[n1i][n1j - 1] = 1;
                    locationMatrix[n1i][n1j - 2] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 + 8, node2, dx, dy + 1);
                }
            } else {
                if (dy != 0 && locationMatrix[n1i][n1j + (dy / Math.abs(dy))] == 0 && locationMatrix[n1i][n1j + 2 * (dy / Math.abs(dy))] == 0) {
                    WirePanel w = new WirePanel(node1, node1 - 8 * dy / Math.abs(dy));
                    frame.add(w);
                    locationMatrix[n1i][n1j + (dy / Math.abs(dy))] = 1;
                    locationMatrix[n1i][n1j + 2 * (dy / Math.abs(dy))] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 - 8 * dy / Math.abs(dy), node2, dx, (dy / Math.abs(dy)) * (Math.abs(dy) - 1));
                } else if (dx != 0 && locationMatrix[n1i + (dx / Math.abs(dx))][n1j] == 0 && locationMatrix[n1i + 2 * (dx / Math.abs(dx))][n1j] == 0) {
                    WirePanel w = new WirePanel(node1, node1 + dx / Math.abs(dx));
                    frame.add(w);
                    locationMatrix[n1i + (dx / Math.abs(dx))][n1j] = 1;
                    locationMatrix[n1i + 2 * (dx / Math.abs(dx))][n1j] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 + dx / Math.abs(dx), node2, (dx / Math.abs(dx)) * (Math.abs(dx) - 1), dy);
                } else if (n1i + 1 <= 13 && locationMatrix[n1i + 1][n1j] == 0 && locationMatrix[n1i + 2][n1j] == 0) {
                    WirePanel w = new WirePanel(node1, node1 + 1);
                    frame.add(w);
                    locationMatrix[n1i + 1][n1j] = 1;
                    locationMatrix[n1i + 2][n1j] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 + 1, node2, dx - 1, dy);
                } else if (n1i - 1 >= 0 && locationMatrix[n1i - 1][n1j] == 0 && locationMatrix[n1i - 2][n1j] == 0) {
                    WirePanel w = new WirePanel(node1, node1 - 1);
                    frame.add(w);
                    locationMatrix[n1i - 1][n1j] = 1;
                    locationMatrix[n1i - 2][n1j] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 - 1, node2, dx + 1, dy);
                } else if (n1j + 1 <= 9 && locationMatrix[n1i][n1j + 1] == 0 && locationMatrix[n1i][n1j + 2] == 0) {
                    WirePanel w = new WirePanel(node1, node1 - 8);
                    frame.add(w);
                    locationMatrix[n1i][n1j + 1] = 1;
                    locationMatrix[n1i][n1j + 2] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 - 8, node2, dx, dy - 1);
                } else if (n1j - 1 >= 0 && locationMatrix[n1i][n1j - 1] == 0 && locationMatrix[n1i][n1j - 2] == 0) {
                    WirePanel w = new WirePanel(node1, node1 + 8);
                    frame.add(w);
                    locationMatrix[n1i][n1j - 1] = 1;
                    locationMatrix[n1i][n1j - 2] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 + 8, node2, dx, dy + 1);
                }
            }
        }
    }
}

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
            TitleBoxImage = ImageIO.read(new File("icons/Circuit title box.png"));
            TitleBoxLabel = new JLabel(new ImageIcon(TitleBoxImage));
            TitleBoxLabel.setBounds(25, 20, 900, 30);
            TitleBoxLabel.setBackground(color);
            TitleLabel = new JLabel("Circuit");
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

class MainFrame extends JFrame {
    MainFrame() {
        setSize(815, 700);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container content = this.getContentPane();
        content.setBackground(Color.DARK_GRAY);
        JRootPane root;
        root = this.getRootPane();
        LayoutManager mgr;
        mgr = new GroupLayout(content);
        this.setLayout(mgr);

        DataPanel d = new DataPanel();
        add(d);

        // CircuitPanel c = new CircuitPanel();
        // add(c);

        ResultPanel r = new ResultPanel(d);
        add(r);

        SharifPanel s = new SharifPanel();
        add(s);
        this.setLocation(450, 100);
        this.setVisible(true);
    }
}

public class Main {
    public static void main(String[] args) throws IOException {
        MainFrame main = new MainFrame();
    }
}
