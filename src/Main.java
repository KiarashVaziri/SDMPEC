import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.Scanner;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;


class Node {
    int nodeNumber;
    float previousVoltage = 0;
    float voltage = 0;
    float newVoltage = 0;
    float previousCurrent = 0;
    float current = 0;
    float newCurrent = 0;

    //for bloody unions
    boolean needs_parent = false;
    int parentNode;
    int unionNumber;
    boolean _added = false;
    Set<Node> neighbors = new HashSet<Node>();
    Branch parentElement;
    int parent_element;

    //-5 error
    boolean connected = false;
    boolean[] neighbor_nodes = new boolean[100];

    Node(int a) {
        nodeNumber = a;
        unionNumber = nodeNumber;
        parentNode = nodeNumber;
    }

    double getVoltage() {
        return voltage;
    }
}

class Ground extends Node {
    static final double voltage = 0;
    Node[] node_array;

    Ground(int a) {
        super(a);
    }
}

class Branch {
    String name;
    int port1, port2;
    boolean independence = true;
    int type = 0;
    float resistance;
    float capacity;
    float inductance;
    float current = 0;
    float previousCurrent = 0;
    float previousCurrent_plus = 0;
    float previousCurrent_negative = 0;
    float newCurrent = 0;
    float voltage;
    float power;

    public Branch(String name, int startNode, int endNode, float value) {
    }

    float getCurrent() {
        return current;
    }

    float getVoltage(Node a, Node b) {
        return voltage;
    }

    void updateBranch(Node startNode, Node endNode, float dt, float dv) {
    }

    void updateBranch(Node[] nodes, float dt, float dv) {
    }

    void updateBranch(Branch[] branches, float dt, float dv) {
    }
}

class Resistor extends Branch {
    Resistor(String name, int a, int b, float r) {
        super(name, a, b, r);
        this.name = name;
        port1 = a;
        port2 = b;
        this.resistance = r;
        //this.voltage = (float) (port1.getVoltage() - port2.getVoltage());
        this.current = voltage / r;
    }

    @Override
    float getVoltage(Node a, Node b) {
        return a.voltage - b.voltage;
    }

    @Override
    void updateBranch(Node startNode, Node endNode, float dt, float dv) {
        current = (startNode.voltage - endNode.voltage) / resistance;
        previousCurrent = (startNode.voltage - endNode.voltage + dv) / resistance;
        previousCurrent_plus = (startNode.voltage - endNode.voltage + dv) / resistance;
        previousCurrent_negative = (startNode.voltage - endNode.voltage - dv) / resistance;
    }
}

class CurrentSource extends Branch {
    float offset;
    float amplitude;
    float frequency;
    float phase;

    CurrentSource(String name, int a, int b, float offset, float amplitude, float frequency, float phase) {
        super(name, a, b, offset);
        this.name = name;
        port1 = b;
        port2 = a;
        //this.voltage = (float) (port1.getVoltage() - port2.getVoltage());
        this.offset = offset;
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.phase = phase;
    }

    @Override
    float getVoltage(Node a, Node b) {
        return b.voltage - a.voltage;
    }

    int timeCS = 1;

    @Override
    void updateBranch(Node a, Node b, float dt, float dv) {
        //System.out.println(offset + amplitude * Math.sin(2 * Math.PI * frequency * k * dt + Math.toRadians(phase)));
        current = (float) (offset + amplitude * Math.sin(2 * Math.PI * frequency * timeCS * dt + Math.toRadians(phase)));
        previousCurrent = current;
        timeCS++;
    }
}

class VoltageSource extends Branch {
    float offset;
    float amplitude;
    float frequency;
    float phase;

    VoltageSource(String name, int a, int b, float offset, float amplitude, float frequency, float phase) {
        super(name, a, b, offset);
        this.name = name;
        port1 = a;
        port2 = b;
        //this.voltage = (float) (port1.getVoltage() - port2.getVoltage());
        this.offset = offset;
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.phase = phase;
    }

    int timeVS = 1;

    void updateBranch(Node s, Node e, float dt, float dv) {
        voltage = (float) (offset + amplitude * Math.sin(2 * Math.PI * frequency * timeVS * dt + Math.toRadians(phase)));
        timeVS++;
    }
}

class Capacitor extends Branch {
    Capacitor(String name, int a, int b, float value) {
        super(name, a, b, value);
        this.name = name;
        port1 = a;
        port2 = b;
        this.capacity = value;
    }

    float getVoltage(Node s, Node e) {
        return s.voltage - e.voltage;
    }

    void updateBranch(Node s, Node e, float dt, float dv) {
        current = (capacity * (s.voltage - s.previousVoltage - e.voltage + e.previousVoltage)) / dt;
        previousCurrent = (capacity * (s.voltage - s.previousVoltage - e.voltage + e.previousVoltage + dv)) / dt;
        previousCurrent_plus = (capacity * (s.voltage - s.previousVoltage - e.voltage + e.previousVoltage + dv)) / dt;
        previousCurrent_negative = (capacity * (s.voltage - s.previousVoltage - e.voltage + e.previousVoltage - dv)) / dt;
    }
}

class Inductor extends Branch {
    Inductor(String name, int a, int b, float value) {
        super(name, a, b, value);
        this.name = name;
        port1 = a;
        port2 = b;
        this.inductance = value;
    }

    float getVoltage(Node s, Node e) {
        return s.voltage - e.voltage;
    }

    void updateBranch(Node s, Node e, float dt, float dv) {
        current += ((s.voltage - s.previousVoltage - e.voltage + e.previousVoltage) * dt) / inductance;
        previousCurrent += ((s.voltage - s.previousVoltage - e.voltage + e.previousVoltage + dv) * dt) / inductance;
    }
}

class Circuit {
    int numberOfNodes = 0;
    int numberOfBranches = 0;
    int numberOfUnions = 0;
    float dt, dv, di = 0.1f;
    int[][] adjMatrix = new int[100][100];
    Node[] nodeArray = new Node[100];
    Branch[] branchArray = new Branch[100];
    Union[] unionArray = new Union[50];

    Circuit(float dt, float dv) {
        this.dt = dt;
        this.dv = dv;
        for (int i = 0; i < 100; i++) for (int j = 0; j < 100; j++) adjMatrix[i][j] = 0;
    }

    Circuit() {
        for (int i = 0; i < 100; i++) for (int j = 0; j < 100; j++) adjMatrix[i][j] = 0;
    }

    class Union {
        Node[] nodes = new Node[10];
        int unionNumber;
        int parent_node;
        int non = 0;

        //for solution
        float newCurrent = 0;
        float current = 0;
        float previousCurrent = 0;

        Union(int a) {
            unionNumber = a;
        }

        void setParentNode(Node node) {
            nodes[0] = node;
            non++;
        }

        void addNode(Node node) {
            nodes[non++] = node;
        }

        void updateVoltages() {
            for (int k = 0; k < non; k++) {
                if (k == 0 && parent_node == 0) {
                    nodeArray[0].voltage = 0;
                    nodeArray[0].previousVoltage = 0;
                } else if (k == 0) {
                    nodes[0].previousVoltage = nodes[0].voltage;
                    nodes[0].voltage -= dv * (Math.abs(newCurrent) - Math.abs(previousCurrent)) / di;
                    //nodes[0].voltage -= dv * newCurrent;
                } else {
                    if (nodes[k].parentNode == nodes[k].parentElement.port1) {
                        nodes[k].previousVoltage = nodes[k].voltage;
                        nodes[k].voltage = nodeArray[nodes[k].parentNode].voltage - nodes[k].parentElement.voltage;
                    } else if (nodes[k].parentNode == nodes[k].parentElement.port2) {
                        nodes[k].previousVoltage = nodes[k].voltage;
                        nodes[k].voltage = nodeArray[nodes[k].parentNode].voltage + nodes[k].parentElement.voltage;
                    }
                }
            }
        }
    }

    void addElement(Branch element) {
        adjMatrix[numberOfBranches][element.port1] = 1;
        adjMatrix[numberOfBranches][element.port2] = -1;
        numberOfNodes = Math.max(numberOfNodes, Math.max(element.port1, element.port2));
        branchArray[numberOfBranches++] = element;
    }

    void initCircuit() {
        for (int j = 0; j <= numberOfNodes; j++) nodeArray[j] = new Node(j);
    }

    void updateBranches() {
        for (int j = 0; branchArray[j] != null; j++) {
            if (branchArray[j].independence)
                branchArray[j].updateBranch(nodeArray[branchArray[j].port1], nodeArray[branchArray[j].port2], dt, dv);
            else if (branchArray[j].type == 1)
                branchArray[j].updateBranch(nodeArray, dt, dv);
            else if (branchArray[j].type == 2)
                branchArray[j].updateBranch(branchArray, dt, dv);
        }
    }

    void updateUnions() {
        for (int k = 0; k < numberOfUnions; k++) {
            unionArray[k].newCurrent = 0;
            unionArray[k].previousCurrent = 0;
            for (int i = 0; unionArray[k].nodes[i] != null; i++) {
                for (int j = 0; j < numberOfBranches; j++) {
                    if (branchArray[j].port1 == unionArray[k].nodes[i].nodeNumber && branchArray[j].getClass() != VoltageSource.class) {
                        unionArray[k].newCurrent += branchArray[j].current;
                        //unionArray[k].previousCurrent += branchArray[j].previousCurrent;
                        unionArray[k].previousCurrent += branchArray[j].previousCurrent_plus;
                    } else if (branchArray[j].port2 == unionArray[k].nodes[i].nodeNumber && branchArray[j].getClass() != VoltageSource.class) {
                        unionArray[k].newCurrent -= branchArray[j].current;
                        //unionArray[k].previousCurrent -= branchArray[j].previousCurrent;
                        unionArray[k].previousCurrent -= branchArray[j].previousCurrent_negative;
                    }
                }
            }
            unionArray[k].current = (unionArray[k].newCurrent + unionArray[k].previousCurrent) / 2;
            unionArray[k].updateVoltages();
        }
    }

    void updateNodes() {
        int cnt = 0;
        for (int i = 0; nodeArray[i] != null; i++) {
            nodeArray[i].newCurrent = 0;
            nodeArray[i].previousCurrent = 0;
            for (int j = 0; branchArray[j] != null && branchArray[i].getClass() != VoltageSource.class; j++) {
                if (branchArray[j].port1 == cnt) {
                    nodeArray[i].newCurrent += branchArray[j].current;
                    nodeArray[i].previousCurrent += branchArray[j].previousCurrent;
                } else if (branchArray[j].port2 == cnt) {
                    nodeArray[i].newCurrent -= branchArray[j].current;
                    nodeArray[i].previousCurrent -= branchArray[j].previousCurrent;
                }
            }
            nodeArray[i].previousVoltage = nodeArray[i].voltage;
            nodeArray[i].current = (nodeArray[i].newCurrent + nodeArray[i].previousCurrent) / 2;

            /*if(nodeArray[i].newCurrent < 0.5);
            else if (nodeArray[i].newCurrent > nodeArray[i].previousCurrent)
                nodeArray[i].voltage += dv;
            else if (nodeArray[i].newCurrent < nodeArray[i].previousCurrent)
                nodeArray[i].voltage -= dv;*/

            nodeArray[i].voltage -= dv * nodeArray[i].newCurrent;
            if (cnt == 0) {
                nodeArray[i].voltage = 0;
                nodeArray[i].previousVoltage = 0;
            }
            cnt++;
        }
    }

    void printData() {
        for (int i = 0; nodeArray[i] != null; i++)
            System.out.println("Node: " + nodeArray[i].nodeNumber + " voltage:" + nodeArray[i].voltage + " current:" + nodeArray[i].current);
        System.out.println();
        for (int k = 0; k < numberOfUnions; k++)
            System.out.println("Union: " + unionArray[k].unionNumber + " current:" + unionArray[k].current);
        System.out.println();
        for (int j = 0; branchArray[j] != null; j++)
            System.out.println("Branch: " + branchArray[j].name + " voltage:" + branchArray[j].getVoltage(nodeArray[branchArray[j].port1], nodeArray[branchArray[j].port2]) + " current:" + branchArray[j].getCurrent());
        System.out.println("--------");
    }

    float aFloat(String str) {
        float result = 0;
        Pattern num = Pattern.compile("[.\\d]+");
        Matcher matcher = num.matcher(str);
        matcher.find();
        if (str.matches("[.\\d]+G")) result = 1000000000 * Float.parseFloat(matcher.group());
        if (str.matches("[.\\d]+M")) result = 1000000 * Float.parseFloat(matcher.group());
        if (str.matches("[.\\d]+k")) result = 1000 * Float.parseFloat(matcher.group());
        if (str.matches("[.\\d]+")) result = Float.parseFloat(matcher.group());
        if (str.matches("[.\\d]+m")) result = 0.001f * Float.parseFloat(matcher.group());
        return result;
    }

    void readFile() throws FileNotFoundException {
        File file = new File("Input.txt");
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            String line;
            String[] info = new String[10];
            line = br.readLine();
            while (line != null) {
                info = line.split("\\s+");
                String element_name = new String(info[0]);
                if (element_name.matches("R(\\d)+")) {
                    int startNode = Integer.parseInt(info[1]);
                    int endNode = Integer.parseInt(info[2]);

                    float value = aFloat(info[3]);
                    Resistor resistor = new Resistor(element_name, startNode, endNode, value);
                    addElement(resistor);
                } else if (element_name.matches("L(\\d)+")) {
                    int startNode = Integer.parseInt(info[1]);
                    int endNode = Integer.parseInt(info[2]);

                    float value = aFloat(info[3]);
                    Inductor inductor = new Inductor(element_name, startNode, endNode, value);
                    addElement(inductor);
                } else if (element_name.matches("C(\\d)+")) {
                    int startNode = Integer.parseInt(info[1]);
                    int endNode = Integer.parseInt(info[2]);

                    float value = aFloat(info[3]);
                    Capacitor capacitor = new Capacitor(element_name, startNode, endNode, value);
                    addElement(capacitor);
                } else if (element_name.matches("I(\\d)+")) {
                    int startNode = Integer.parseInt(info[1]);
                    int endNode = Integer.parseInt(info[2]);

                    float offset = aFloat(info[3]);
                    float amplitude = aFloat(info[4]);
                    float frequency = aFloat(info[5]);
                    float phase = Float.parseFloat(info[6]);
                    CurrentSource cs = new CurrentSource(element_name, startNode, endNode, offset, amplitude, frequency, phase);
                    addElement(cs);
                } else if (element_name.matches("V(\\d)+")) {
                    int startNode = Integer.parseInt(info[1]);
                    int endNode = Integer.parseInt(info[2]);

                    float offset = aFloat(info[3]);
                    float amplitude = aFloat(info[4]);
                    float frequency = aFloat(info[5]);
                    float phase = Float.parseFloat(info[6]);
                    VoltageSource vs = new VoltageSource(element_name, startNode, endNode, offset, amplitude, frequency, phase);
                    addElement(vs);
                } else if (element_name.matches("G(\\d)+")) {
                    int startNode = Integer.parseInt(info[1]);
                    int endNode = Integer.parseInt(info[2]);

                    int related_port1 = Integer.parseInt(info[3]);
                    int related_port2 = Integer.parseInt(info[4]);
                    float gain = aFloat(info[5]);
                    VoltageDependentCS voltageDependentCS = new VoltageDependentCS(element_name, startNode, endNode, related_port1, related_port2, gain);
                    addElement(voltageDependentCS);
                } else if (element_name.matches("F(\\d)+")) {
                    int startNode = Integer.parseInt(info[1]);
                    int endNode = Integer.parseInt(info[2]);

                    String dependentElementName = info[3];
                    float gain = aFloat(info[4]);
                    CurrentDependentCS currentDependentCS = new CurrentDependentCS(element_name, startNode, endNode, dependentElementName, gain);
                    addElement(currentDependentCS);
                } else if (element_name.matches("dv")) dv = Float.parseFloat(info[1]);
                else if (element_name.matches("dt")) dt = Float.parseFloat(info[1]);
                else if (element_name.matches("di")) di = Float.parseFloat(info[1]);
                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void makeNeighbors(int startNode, int endNode) {
        nodeArray[startNode].neighbors.add(nodeArray[endNode]);
        nodeArray[endNode].neighbors.add(nodeArray[startNode]);
    }

    void makeNeighbors() {
        initCircuit();
        int start = 0, end = 0;
        for (int j = 0; j < numberOfBranches; j++) {
            if (branchArray[j].getClass() == VoltageSource.class) {
                for (int i = 0; i <= numberOfNodes; i++) {
                    if (adjMatrix[j][i] == 1) start = i;
                    if (adjMatrix[j][i] == -1) end = i;
                }
                makeNeighbors(start, end);
                nodeArray[start].needs_parent = true;
                nodeArray[end].needs_parent = true;
            }
        }
    }

    void neighborFunction(int index) {
        nodeArray[index]._added = true;
        for (Node neighbor : nodeArray[index].neighbors) {
            if (!neighbor._added) {
                neighbor.parentNode = index;
                neighbor.unionNumber = nodeArray[index].unionNumber;
                neighborFunction(neighbor.nodeNumber);
            }
        }
    }

    void setUnions() {
        makeNeighbors();
        for (int i = 0; i <= numberOfNodes; i++) if (!nodeArray[i].needs_parent) nodeArray[i]._added = true;

        for (int i = 0; i <= numberOfNodes; i++) {
            if (!nodeArray[i]._added) neighborFunction(i);
        }
    }

    void setParentElements() {
        setUnions();
        int start = 0, end = 0;
        for (int j = 0; j < numberOfBranches; j++) {
            if (branchArray[j].getClass() == VoltageSource.class) {
                for (int i = 0; i <= numberOfNodes; i++) {
                    if (adjMatrix[j][i] == 1) start = i;
                    if (adjMatrix[j][i] == -1) end = i;
                }
                if (nodeArray[start].parentNode == end) {
                    nodeArray[start].parent_element = j;
                    nodeArray[start].parentElement = branchArray[j];
                } else if (nodeArray[end].parentNode == start) {
                    nodeArray[end].parent_element = j;
                    nodeArray[end].parentElement = branchArray[j];
                }
            }
        }
    }

    void gatherUnions() {
        setParentElements();
        for (int i = 0; nodeArray[i] != null; i++) {
            if (nodeArray[i].unionNumber == i) {
                unionArray[numberOfUnions] = new Union(numberOfUnions);
                unionArray[numberOfUnions].setParentNode(nodeArray[i]);
                unionArray[numberOfUnions++].parent_node = i;
            }
        }
        for (int i = 0; i <= numberOfNodes; i++) {
            if (nodeArray[i].unionNumber != i)
                for (int k = 0; k < numberOfUnions; k++)
                    if (nodeArray[i].unionNumber == unionArray[k].parent_node)
                        unionArray[k].addNode(nodeArray[i]);
        }
    }

    boolean checkIfConnected() {
        boolean flag = true;
        nodeArray[0].connected = true;
        set_connections(0);
        for (int i = 0; i <= numberOfNodes && nodeArray[i].connected == false; i++) flag = false;
        return flag;
    }

    void set_connections(int node_number) {
        for (int j = 0; j < numberOfBranches; j++) {
            if (adjMatrix[j][node_number] == 1 || adjMatrix[j][node_number] == -1) {
                for (int i = 0; i <= numberOfNodes; i++) {
                    if ((adjMatrix[j][i] == 1 || adjMatrix[j][i] == -1) && nodeArray[i].connected == false) {
                        nodeArray[i].connected = true;
                        set_connections(i);
                    }

                }
            }
        }
    }
}

//Phase two
class ResistorPanel extends JPanel
{
    int node1x , node1y , node2x , node2y;
    ResistorPanel(int node1 , int node2)
    {
        node1x = ((node1 - 1) % 6) * 100 + 50;
        node1y = 450 - (node1 - 1) / 6 * 100;
        node2x = ((node2 - 1) % 6) * 100 + 50;
        node2y = 450 - (node2 - 1) / 6 * 100;
        System.out.printf("%d     %d\n%d     %d#\n" ,  node1x , node1y , node2x , node2y);
        setBounds((node1x + node2x) / 2 - 50 , (node1y + node2y) / 2 - 55 , 100 ,105);
        Color color = new Color(1 , 1  ,1 , 1);
        setBackground(color);
        BufferedImage ResistorImage;
        JLabel ResistorLabel;
        if(node1x == node2x)
        {
            try
            {
                ResistorImage = ImageIO.read(new File("icons/Vertical Resistor.png"));
                ResistorLabel = new JLabel(new ImageIcon(ResistorImage));
                add(ResistorLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(node1y == node2y)
        {
            try
            {
                ResistorImage = ImageIO.read(new File("icons/Horizontal Resistor.png"));
                ResistorLabel = new JLabel(new ImageIcon(ResistorImage));
                add(ResistorLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
class CapacitorPanel extends JPanel
{
    int node1x , node1y , node2x , node2y;
    CapacitorPanel(int node1 , int node2)
    {
        node1x = ((node1 - 1) % 6) * 100 + 50;
        node1y = 450 - (node1 - 1) / 6 * 100;
        node2x = ((node2 - 1) % 6) * 100 + 50;
        node2y = 450 - (node2 - 1) / 6 * 100;
        setBounds((node1x + node2x) / 2 - 50 , (node1y + node2y) / 2 - 55 , 100 ,105);
        Color color = new Color(1 , 1  ,1 , 1);
        setBackground(color);
        BufferedImage CapacitorImage;
        JLabel CapacitorLabel;
        if(node1x == node2x)
        {
            try
            {
                CapacitorImage = ImageIO.read(new File("icons/Vertical Capacitor.png"));
                CapacitorLabel = new JLabel(new ImageIcon(CapacitorImage));
                add(CapacitorLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(node1y == node2y)
        {
            try
            {
                CapacitorImage = ImageIO.read(new File("icons/Horizontal Capacitor.png"));
                CapacitorLabel = new JLabel(new ImageIcon(CapacitorImage));
                add(CapacitorLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
class InductorPanel extends JPanel
{
    int node1x , node1y , node2x , node2y;
    InductorPanel(int node1 , int node2)
    {
        node1x = ((node1 - 1) % 6) * 100 + 50;
        node1y = 450 - (node1 - 1) / 6 * 100;
        node2x = ((node2 - 1) % 6) * 100 + 50;
        node2y = 450 - (node2 - 1) / 6 * 100;
        setBounds((node1x + node2x) / 2 - 50 , (node1y + node2y) / 2 - 55 , 100 ,105);
        Color color = new Color(1 , 1  ,1 , 1);
        setBackground(color);
        BufferedImage InductorImage;
        JLabel InductorLabel;
        if(node1x == node2x)
        {
            try
            {
                InductorImage = ImageIO.read(new File("icons/Vertical Inductor.png"));
                InductorLabel = new JLabel(new ImageIcon(InductorImage));
                add(InductorLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(node1y == node2y)
        {
            try
            {
                InductorImage = ImageIO.read(new File("icons/Horizontal Inductor.png"));
                InductorLabel = new JLabel(new ImageIcon(InductorImage));
                add(InductorLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
class VoltageDCPanel extends  JPanel
{
    int node1x , node1y , node2x , node2y;
    VoltageDCPanel(int node1 , int node2)
    {
        node1x = ((node1 - 1) % 6) * 100 + 50;
        node1y = 450 - (node1 - 1) / 6 * 100;
        node2x = ((node2 - 1) % 6) * 100 + 50;
        node2y = 450 - (node2 - 1) / 6 * 100;
        setBounds((node1x + node2x) / 2 - 50 , (node1y + node2y) / 2 - 55 , 100 ,105);
        Color color = new Color(1 , 1  ,1 , 1);
        setBackground(color);
        BufferedImage VoltageDCImage;
        JLabel VoltageDCLabel;
        if(node1x == node2x && node1y > node2y)
        {
            try
            {
                VoltageDCImage = ImageIO.read(new File("icons/Up side VoltageDC.png"));
                VoltageDCLabel = new JLabel(new ImageIcon(VoltageDCImage));
                add(VoltageDCLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(node1x == node2x && node2y > node1y)
        {
            try
            {
                VoltageDCImage = ImageIO.read(new File("icons/Down side VoltageDC.png"));
                VoltageDCLabel = new JLabel(new ImageIcon(VoltageDCImage));
                add(VoltageDCLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(node1y == node2y && node1x < node2x)
        {
            try
            {
                VoltageDCImage = ImageIO.read(new File("icons/Right side VoltageDC.png"));
                VoltageDCLabel = new JLabel(new ImageIcon(VoltageDCImage));
                add(VoltageDCLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(node1y == node2y && node1x > node2x)
        {
            try
            {
                VoltageDCImage = ImageIO.read(new File("icons/Left side VoltageDC.png"));
                VoltageDCLabel = new JLabel(new ImageIcon(VoltageDCImage));
                add(VoltageDCLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
class VoltageACPanel extends  JPanel
{
    int node1x , node1y , node2x , node2y;
    VoltageACPanel(int node1 , int node2)
    {
        node1x = ((node1 - 1) % 6) * 100 + 50;
        node1y = 450 - (node1 - 1) / 6 * 100;
        node2x = ((node2 - 1) % 6) * 100 + 50;
        node2y = 450 - (node2 - 1) / 6 * 100;
        setBounds((node1x + node2x) / 2 - 50 , (node1y + node2y) / 2 - 55 , 100 ,105);
        Color color = new Color(1 , 1  ,1 , 1);
        setBackground(color);
        BufferedImage VoltageACImage;
        JLabel VoltageACLabel;
        if(node1x == node2x && node1y > node2y)
        {
            try
            {
                VoltageACImage = ImageIO.read(new File("icons/Up side VoltageAC.png"));
                VoltageACLabel = new JLabel(new ImageIcon(VoltageACImage));
                add(VoltageACLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(node1x == node2x && node2y > node1y)
        {
            try
            {
                VoltageACImage = ImageIO.read(new File("icons/Down side VoltageAC.png"));
                VoltageACLabel = new JLabel(new ImageIcon(VoltageACImage));
                add(VoltageACLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(node1y == node2y && node1x < node2x)
        {
            try
            {
                VoltageACImage = ImageIO.read(new File("icons/Right side VoltageAC.png"));
                VoltageACLabel = new JLabel(new ImageIcon(VoltageACImage));
                add(VoltageACLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(node1y == node2y && node1x > node2x)
        {
            try
            {
                VoltageACImage = ImageIO.read(new File("icons/Left side VoltageAC.png"));
                VoltageACLabel = new JLabel(new ImageIcon(VoltageACImage));
                add(VoltageACLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
class CurrentDCPanel extends  JPanel
{
    int node1x , node1y , node2x , node2y;
    CurrentDCPanel(int node1 , int node2)
    {
        node1x = ((node1 - 1) % 6) * 100 + 50;
        node1y = 450 - (node1 - 1) / 6 * 100;
        node2x = ((node2 - 1) % 6) * 100 + 50;
        node2y = 450 - (node2 - 1) / 6 * 100;
        setBounds((node1x + node2x) / 2 - 50 , (node1y + node2y) / 2 - 55 , 100 ,105);
        Color color = new Color(1 , 1  ,1 , 1);
        setBackground(color);
        BufferedImage CurrentDCImage;
        JLabel CurrentDCLabel;
        if(node1x == node2x && node1y > node2y)
        {
            try
            {
                CurrentDCImage = ImageIO.read(new File("icons/Up side CurrentDC.png"));
                CurrentDCLabel = new JLabel(new ImageIcon(CurrentDCImage));
                add(CurrentDCLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(node1x == node2x && node2y > node1y)
        {
            try
            {
                CurrentDCImage = ImageIO.read(new File("icons/Down side CurrentDC.png"));
                CurrentDCLabel = new JLabel(new ImageIcon(CurrentDCImage));
                add(CurrentDCLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(node1y == node2y && node1x < node2x)
        {
            try
            {
                CurrentDCImage = ImageIO.read(new File("icons/Right side CurrentDC.png"));
                CurrentDCLabel = new JLabel(new ImageIcon(CurrentDCImage));
                add(CurrentDCLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(node1y == node2y && node1x > node2x)
        {
            try
            {
                CurrentDCImage = ImageIO.read(new File("icons/Left side CurrentDC.png"));
                CurrentDCLabel = new JLabel(new ImageIcon(CurrentDCImage));
                add(CurrentDCLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
class CurrentACPanel extends  JPanel
{
    int node1x , node1y , node2x , node2y;
    CurrentACPanel(int node1 , int node2)
    {
        node1x = ((node1 - 1) % 6) * 100 + 50;
        node1y = 450 - (node1 - 1) / 6 * 100;
        node2x = ((node2 - 1) % 6) * 100 + 50;
        node2y = 450 - (node2 - 1) / 6 * 100;
        setBounds((node1x + node2x) / 2 - 50 , (node1y + node2y) / 2 - 55 , 100 ,105);
        Color color = new Color(1 , 1  ,1 , 1);
        setBackground(color);
        BufferedImage CurrentACImage;
        JLabel CurrentACLabel;
        if(node1x == node2x && node1y > node2y)
        {
            try
            {
                CurrentACImage = ImageIO.read(new File("icons/Up side CurrentAC.png"));
                CurrentACLabel = new JLabel(new ImageIcon(CurrentACImage));
                add(CurrentACLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(node1x == node2x && node2y > node1y)
        {
            try
            {
                CurrentACImage = ImageIO.read(new File("icons/Down side CurrentAC.png"));
                CurrentACLabel = new JLabel(new ImageIcon(CurrentACImage));
                add(CurrentACLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(node1y == node2y && node1x < node2x)
        {
            try
            {
                CurrentACImage = ImageIO.read(new File("icons/Right side CurrentAC.png"));
                CurrentACLabel = new JLabel(new ImageIcon(CurrentACImage));
                add(CurrentACLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(node1y == node2y && node1x > node2x)
        {
            try
            {
                CurrentACImage = ImageIO.read(new File("icons/Left side CurrentAC.png"));
                CurrentACLabel = new JLabel(new ImageIcon(CurrentACImage));
                add(CurrentACLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
class DependentCurrentPanel extends  JPanel
{
    int node1x , node1y , node2x , node2y;
    DependentCurrentPanel(int node1 , int node2)
    {
        node1x = ((node1 - 1) % 6) * 100 + 50;
        node1y = 450 - (node1 - 1) / 6 * 100;
        node2x = ((node2 - 1) % 6) * 100 + 50;
        node2y = 450 - (node2 - 1) / 6 * 100;
        setBounds((node1x + node2x) / 2 - 50 , (node1y + node2y) / 2 - 55 , 100 ,105);
        Color color = new Color(1 , 1  ,1 , 1);
        setBackground(color);
        BufferedImage DependentCurrentImage;
        JLabel DependentCurrentLabel;
        if(node1x == node2x && node1y > node2y)
        {
            try
            {
                DependentCurrentImage = ImageIO.read(new File("icons/Up side DependentCurrent.png"));
                DependentCurrentLabel = new JLabel(new ImageIcon(DependentCurrentImage));
                add(DependentCurrentLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(node1x == node2x && node2y > node1y)
        {
            try
            {
                DependentCurrentImage = ImageIO.read(new File("icons/Down side DependentCurrent.png"));
                DependentCurrentLabel = new JLabel(new ImageIcon(DependentCurrentImage));
                add(DependentCurrentLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(node1y == node2y && node1x < node2x)
        {
            try
            {
                DependentCurrentImage = ImageIO.read(new File("icons/Right side DependentCurrent.png"));
                DependentCurrentLabel = new JLabel(new ImageIcon(DependentCurrentImage));
                add(DependentCurrentLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(node1y == node2y && node1x > node2x)
        {
            try
            {
                DependentCurrentImage = ImageIO.read(new File("icons/Left side DependentCurrent.png"));
                DependentCurrentLabel = new JLabel(new ImageIcon(DependentCurrentImage));
                add(DependentCurrentLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
class DependentVoltagePanel extends  JPanel
{
    int node1x , node1y , node2x , node2y;
    DependentVoltagePanel(int node1 , int node2)
    {
        node1x = ((node1 - 1) % 6) * 100 + 50;
        node1y = 450 - (node1 - 1) / 6 * 100;
        node2x = ((node2 - 1) % 6) * 100 + 50;
        node2y = 450 - (node2 - 1) / 6 * 100;
        setBounds((node1x + node2x) / 2 - 50 , (node1y + node2y) / 2 - 55 , 100 ,105);
        Color color = new Color(1 , 1  ,1 , 1);
        setBackground(color);
        BufferedImage DependentVoltageImage;
        JLabel DependentVoltageLabel;
        if(node1x == node2x && node1y > node2y)
        {
            try
            {
                DependentVoltageImage = ImageIO.read(new File("icons/Up side DependentVoltage.png"));
                DependentVoltageLabel = new JLabel(new ImageIcon(DependentVoltageImage));
                add(DependentVoltageLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(node1x == node2x && node2y > node1y)
        {
            try
            {
                DependentVoltageImage = ImageIO.read(new File("icons/Down side DependentVoltage.png"));
                DependentVoltageLabel = new JLabel(new ImageIcon(DependentVoltageImage));
                add(DependentVoltageLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(node1y == node2y && node1x < node2x)
        {
            try
            {
                DependentVoltageImage = ImageIO.read(new File("icons/Right side DependentVoltage.png"));
                DependentVoltageLabel = new JLabel(new ImageIcon(DependentVoltageImage));
                add(DependentVoltageLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(node1y == node2y && node1x > node2x)
        {
            try
            {
                DependentVoltageImage = ImageIO.read(new File("icons/Left side DependentVoltage.png"));
                DependentVoltageLabel = new JLabel(new ImageIcon(DependentVoltageImage));
                add(DependentVoltageLabel);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
class GNDPanel extends JPanel
{
    int nodex , nodey;
    GNDPanel(int node)
    {
        nodex = ((node - 1) % 6) * 100 + 50;
        nodey = 450 - (node - 1) / 6 * 100;
        setBounds(nodex , nodey - 5 , 100 , 100);
        Color color = new Color(1 , 1  ,1 , 1);
        setBackground(color);
        BufferedImage GNDImage;
        JLabel GNDLabel;
        try
        {
            GNDImage = ImageIO.read(new File("icons/GND.png"));
            GNDLabel = new JLabel(new ImageIcon(GNDImage));
            add(GNDLabel);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
class WirePanel extends JPanel
{
    int node1x , node1y , node2x , node2y;
    WirePanel(int node1 , int node2)
    {
        node1x = ((node1 - 1) % 6) * 100 + 50;
        node1y = 450 - (node1 - 1) / 6 * 100;
        node2x = ((node2 - 1) % 6) * 100 + 50;
        node2y = 450 - (node2 - 1) / 6 * 100;
        setBounds((node1x + node2x) / 2 - 50 , (node1y + node2y) / 2 - 50 , 100 ,100);
        Color color = new Color(1 , 1  ,1 , 1);
        setBackground(color);
    }
    public void paint(Graphics g)
    {
        Graphics2D graphics = (Graphics2D) g;
        graphics.setStroke(new BasicStroke(2));
        graphics.setColor(Color.BLACK);
        if(node1x == node2x)
            graphics.drawLine(50 , 0 , 50 , 100);
        else if(node1y == node2y)
            graphics.drawLine(0 , 50 , 100 , 50);
    }
}
class Element
{
    int dx , dy , n1i , n1j , n2i , n2j;
    Element(JComponent frame , int locationMatrix [][] , char type , int node1 , int node2)
    {
        dx = (node2 - 1) % 6 - (node1 - 1) % 6;
        dy = (node1 - 1) / 6 - (node2 - 1) / 6;
        n1i = (node1 - 1) % 6;
        n1j = 4 - (node1 - 1) / 6;
        n2i = (node2 - 1) % 6;
        n2j = 4 - (node2 - 1) / 6;
        if(node1 != 0 && node2 != 0)
        {
            locationMatrix[n1i][n1j] = 1;
            locationMatrix[n2i][n2j] = 1;
        }
        Draw(frame , locationMatrix , type , node1 , node2 , dx , dy);
    }
    void Draw(JComponent frame , int locationMatrix [][] , char type , int node1 , int node2 , int dx , int dy)
    {
        int n1i , n1j;
        n1i = (node1 - 1) % 6;
        n1j = 4 - (node1 - 1) / 6;
        n2i = (node2 - 1) % 6;
        n2j = 4 - (node2 - 1) / 6;
        if(node1 == 0)
        {
            if(n2i + 1 <= 5 && locationMatrix[n2i + 1][n2j] == 0)
                node1 = node2 + 1;
            else if(n2i - 1 >= 0 && locationMatrix[n2i - 1][n2j] == 0)
                node1 = node2 - 1;
            else if(n2j + 1 <= 4 && locationMatrix[n2i][n2j + 1] == 0)
                node1 = node2 - 6;
            else if(n2j - 1 >= 0 && locationMatrix[n2i][n2j - 1] == 0)
                node1 = node2 + 6;
            GNDPanel g = new GNDPanel(node1);
            frame.add(g);
            Draw(frame , locationMatrix , type , node1 , node2 , dx , dy);
        }
        else if(node2 == 0)
        {
            if(n1i + 1 <= 5 && locationMatrix[n1i + 1][n1j] == 0)
                node2 = node1 + 1;
            else if(n1i - 1 >= 0 && locationMatrix[n1i - 1][n1j] == 0)
                node2 = node1 - 1;
            else if(n1j + 1 <= 4 && locationMatrix[n1i][n1j + 1] == 0)
                node2 = node1 - 6;
            else if(n1j - 1 >= 0 && locationMatrix[n1i][n1j - 1] == 0)
                node2 = node1 + 6;
            GNDPanel g = new GNDPanel(node2);
            frame.add(g);
            Draw(frame , locationMatrix , type , node1 , node2 , dx , dy);
        }
        else if((Math.abs(n1i - n2i) == 1 && n1j == n2j) || (Math.abs(n1j - n2j) == 1 && n1i == n2i))
        {
            if(type == 'R')
            {
                ResistorPanel r = new ResistorPanel(node1 , node2);
                frame.add(r);
            }
            else if(type == 'C')
            {
                CapacitorPanel c = new CapacitorPanel(node1 , node2);
                frame.add(c);
            }
            else if(type == 'L')
            {
                InductorPanel l = new InductorPanel(node1 , node2);
                frame.add(l);
            }
            else if(type == 'I')
            {
                CurrentDCPanel i = new CurrentDCPanel(node1 , node2);
                frame.add(i);
            }
            else if(type == 'V')
            {
                VoltageDCPanel v = new VoltageDCPanel(node1 , node2);
                frame.add(v);
            }
            else if(type == 'G')
            {
                DependentVoltagePanel g = new DependentVoltagePanel(node1 , node2);
                frame.add(g);
            }
            else if(type == 'F')
            {
                DependentVoltagePanel f = new DependentVoltagePanel(node1 , node2);
                frame.add(f);
            }
            else if(type == 'H')
            {
                DependentCurrentPanel h = new DependentCurrentPanel(node1 , node2);
                frame.add(h);
            }
            else if(type == 'E')
            {
                DependentCurrentPanel e = new DependentCurrentPanel(node1 , node2);
                frame.add(e);
            }
        }
        else
        {
            if(dx == 0)
            {
                if(dy != 0 && locationMatrix[n1i][n1j + (dy / Math.abs(dy))] == 0)
                {
                    WirePanel w = new WirePanel(node1 , node1 - 6 * dy / Math.abs(dy));
                    frame.add(w);
                    locationMatrix[n1i][n1j + (dy / Math.abs(dy))] = 1;
                    Draw(frame , locationMatrix , type , node1 - 6 * dy / Math.abs(dy) , node2 , dx , (dy / Math.abs(dy)) * (Math.abs(dy) - 1));
                }
                else if(locationMatrix[n1i + 1][n1j] == 0 && n1i + 1 <= 5)
                {
                    WirePanel w = new WirePanel(node1 , node1 + 1);
                    frame.add(w);
                    locationMatrix[n1i + 1][n1j] = 1;
                    Draw(frame , locationMatrix , type , node1 + 1 , node2 , dx - 1 , dy);
                }
                else if(n1i - 1 >= 0 && locationMatrix[n1i - 1][n1j] == 0)
                {
                    WirePanel w = new WirePanel(node1 , node1 - 1);
                    frame.add(w);
                    locationMatrix[n1i - 1][n1j] = 1;
                    Draw(frame , locationMatrix , type , node1 - 1 , node2 , dx + 1 , dy);
                }
            }
            else if(dy == 0)
            {
                System.out.printf("%d    %d\n" , n1j , locationMatrix[n1i + (dx / Math.abs(dx))][n1j]);
                if(dx != 0 && locationMatrix[n1i + (dx / Math.abs(dx))][n1j] == 0)
                {
                    WirePanel w = new WirePanel(node1 , node1 + dx / Math.abs(dx));
                    frame.add(w);
                    locationMatrix[n1i + (dx / Math.abs(dx))][n1j] = 1;
                    Draw(frame , locationMatrix , type , node1 + dx / Math.abs(dx) , node2 , (dx / Math.abs(dx)) * (Math.abs(dx) - 1) , dy);
                }
                else if(n1j + 1 <= 4 && locationMatrix[n1i][n1j + 1] == 0)
                {
                    WirePanel w = new WirePanel(node1 , node1 - 6);
                    frame.add(w);
                    locationMatrix[n1i][n1j + 1] = 1;
                    Draw(frame , locationMatrix , type , node1 - 6 , node2 , dx , dy - 1);
                }
                else if(n1j - 1 >= 0 && locationMatrix[n1i][n1j - 1] == 0)
                {
                    WirePanel w = new WirePanel(node1 , node1 + 6);
                    frame.add(w);
                    locationMatrix[n1i][n1j - 1] = 1;
                    Draw(frame , locationMatrix , type , node1 + 6 , node2 , dx , dy + 1);
                }
            }
            else
            {
                if(dy != 0 && locationMatrix[n1i][n1j + (dy / Math.abs(dy))] == 0)
                {
                    WirePanel w = new WirePanel(node1 , node1 - 6 * dy / Math.abs(dy));
                    frame.add(w);
                    locationMatrix[n1i][n1j + (dy / Math.abs(dy))] = 1;
                    Draw(frame , locationMatrix , type , node1 - 6 * dy / Math.abs(dy) , node2 , dx , (dy / Math.abs(dy)) * (Math.abs(dy) - 1));
                }
                else if(dx != 0 && locationMatrix[n1i + (dx / Math.abs(dx))][n1j] == 0)
                {
                    WirePanel w = new WirePanel(node1 , node1 + dx / Math.abs(dx));
                    frame.add(w);
                    locationMatrix[n1i + (dx / Math.abs(dx))][n1j] = 1;
                    Draw(frame , locationMatrix , type , node1 + dx / Math.abs(dx) , node2 , (dx / Math.abs(dx)) * (Math.abs(dx) - 1) , dy);
                }
                else if( n1i + 1 <= 5 && locationMatrix[n1i + 1][n1j] == 0)
                {
                    WirePanel w = new WirePanel(node1 , node1 + 1);
                    frame.add(w);
                    locationMatrix[n1i + 1][n1j] = 1;
                    Draw(frame , locationMatrix , type , node1 + 1 , node2 , dx - 1 , dy);
                }
                else if(n1i - 1 >= 0 && locationMatrix[n1i - 1][n1j] == 0)
                {
                    WirePanel w = new WirePanel(node1 , node1 - 1);
                    frame.add(w);
                    locationMatrix[n1i - 1][n1j] = 1;
                    Draw(frame , locationMatrix , type , node1 - 1 , node2 , dx + 1 , dy);
                }
                else if(n1j + 1 <= 4 && locationMatrix[n1i][n1j + 1] == 0)
                {
                    WirePanel w = new WirePanel(node1 , node1 - 6);
                    frame.add(w);
                    locationMatrix[n1i][n1j + 1] = 1;
                    Draw(frame , locationMatrix , type , node1 - 6 , node2 , dx , dy - 1);
                }
                else if(n1j - 1 >= 0 && locationMatrix[n1i][n1j - 1] == 0)
                {
                    WirePanel w = new WirePanel(node1 , node1 + 6);
                    frame.add(w);
                    locationMatrix[n1i][n1j - 1] = 1;
                    Draw(frame , locationMatrix , type , node1 + 6 , node2 , dx , dy + 1);
                }
            }
        }
    }
}
class DataPanel extends JComponent implements ActionListener
{
    JButton load_button, save_button;
    JTextArea textArea;
    Border button_border , textArea_border;
    String filePath;

    DataPanel() {
        setBounds(25 , 25 , 350 , 600);

        button_border = BorderFactory.createLineBorder(Color.BLACK , 1);
        load_button = new JButton("     Load");
        load_button.addActionListener(this);
        int x_load_button = 50;
        int y_load_button = 530;
        int width_load_button = 100;
        int height_load_button = 30;
        load_button.setBounds(x_load_button, y_load_button, width_load_button, height_load_button);
        load_button.setBackground(Color.ORANGE);
        load_button.setBorder(button_border);
        Image Load_ButtonImage;
        JLabel Load_ButtonLabel;
        try
        {
            Load_ButtonImage = ImageIO.read(new File("icons/Load Button.png"));
            Load_ButtonLabel = new JLabel(new ImageIcon(Load_ButtonImage));
            load_button.add(Load_ButtonLabel);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        add(load_button);

        save_button = new JButton("     Save");
        save_button.addActionListener(this);
        int x_run_button = x_load_button + width_load_button + 50;
        int y_run_button = 530;
        int width_run_button = 100;
        int height_run_button = 30;
        save_button.setBounds(x_run_button, y_run_button, width_run_button, height_run_button);
        save_button.setBackground(Color.ORANGE);
        save_button.setBorder(button_border);
        Image Save_ButtonImage;
        JLabel Save_ButtonLabel;
        try
        {
            Save_ButtonImage = ImageIO.read(new File("icons/Save Button.png"));
            Save_ButtonLabel = new JLabel(new ImageIcon(Save_ButtonImage));
            save_button.add(Save_ButtonLabel);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        add(save_button);

        textArea = new JTextArea(450, 300);
        textArea.setBounds(0, 0, 350, 500);
        textArea.setBackground(Color.WHITE);
        textArea_border = BorderFactory.createLineBorder(Color.black, 2);
        textArea.setBorder(textArea_border);
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
            textArea.setFont(textArea.getFont().deriveFont(20f));
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
class CircuitPanel extends JComponent
{
    int a[][] = new int[10][10];
    int startNode , endNode;
    int locationMatrix[][] = new int[6][5];
    int adjMatrix[][];
    Branch branchArray[];
    CircuitPanel(int [][] matrix , Branch[] Array)
    {
        adjMatrix = matrix;
        branchArray = Array;
        setBounds(800 , 25 , 700 , 600);
        setBackground(Color.WHITE);
        Border border = BorderFactory.createLineBorder(Color.BLACK , 2);
        setBorder(border);
        for(int i = 0 ; i < 6 ; i++)
        {
            for(int j = 0 ; j < 5 ; j++)
                locationMatrix[i][j] = 0;
        }
    }
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        Graphics2D g2d = (Graphics2D) g;
    }
    void DrawCircuit()
    {
        for(int j = 0 ; j < adjMatrix[0].length ; j++)
        {
            for(int i = 0 ; i < adjMatrix.length ; i++)
            {
                if(adjMatrix[i][j] == 1)
                    startNode = i;
                else if(adjMatrix[i][j] == -1)
                    endNode = i;
            }
            new Element(this , locationMatrix , branchArray[j].name.charAt(0) , startNode , endNode);
        }
    }
}
class ResultPanel extends JComponent implements ActionListener
{
    JButton run_button , draw_button;
    JTextArea ResultArea;
    // adjMatrix bayad be resultpanel dade shavad
    int [][] adjMatrix;
    Branch branchArray[];
    CircuitPanel c;
    ResultPanel (int [][] Matrix , Branch [] Array)
    {
        adjMatrix = Matrix;
        branchArray = Array;
        setBounds(400 , 25 , 350 , 600);
        Border button_border = BorderFactory.createLineBorder(Color.BLACK , 1);
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
        try
        {
            Run_ButtonImage = ImageIO.read(new File("icons/Run Button.png"));
            Run_ButtonLabel = new JLabel(new ImageIcon(Run_ButtonImage));
            run_button.add(Run_ButtonLabel);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        add(run_button);

        draw_button = new JButton("     Draw");
        draw_button.addActionListener(this);
        int x_run_button = x_load_button + width_load_button + 50;
        int y_run_button = 530;
        int width_run_button = 100;
        int height_run_button = 30;
        draw_button.setBounds(x_run_button, y_run_button, width_run_button, height_run_button);
        draw_button.setBackground(Color.ORANGE);
        draw_button.setBorder(button_border);
        Image Draw_ButtonImage;
        JLabel Draw_ButtonLabel;
        try
        {
            Draw_ButtonImage = ImageIO.read(new File("icons/Draw Button.png"));
            Draw_ButtonLabel = new JLabel(new ImageIcon(Draw_ButtonImage));
            draw_button.add(Draw_ButtonLabel);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        add(draw_button);

        ResultArea = new JTextArea(450, 300);
        ResultArea.setBounds(0, 0, 350, 500);
        ResultArea.setBackground(Color.WHITE);
        Border ResultArea_border = BorderFactory.createLineBorder(Color.black, 2);
        ResultArea.setBorder(ResultArea_border);
        add(ResultArea);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == draw_button)
        {
            c = new CircuitPanel(adjMatrix , branchArray);
        }
    }
}
class SharifPanel extends JPanel
{
    Image SharifImage;
    JLabel SharifLabel;
    SharifPanel()
    {
        Color color = new Color(1 , 1 , 1 , 1);
        setBounds(0 , 650 , 150 , 150);
        setBackground(color);
        try
        {
            SharifImage = ImageIO.read(new File("icons/Sharif Icon.png"));
            SharifLabel = new JLabel(new ImageIcon(SharifImage));
            add(SharifLabel);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
class MainPanel extends JFrame
{
    MainPanel(Circuit circuit)
    {
        setSize(1550 , 800);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container content = this.getContentPane();
        content.setBackground(Color.DARK_GRAY);
        JRootPane root;
        root = this.getRootPane();
        LayoutManager mgr;
        mgr = new GroupLayout(content);
        setLayout(mgr);

        DataPanel d = new DataPanel();
        add(d);

        //CircuitPanel c = new CircuitPanel();
        //add(c);

        ResultPanel r = new ResultPanel(circuit.adjMatrix , circuit.branchArray);
        add(r);

        SharifPanel s = new SharifPanel();
        add(s);
        setVisible(true);
    }
}

public class Main {
    public static void main(String[] args) {
        Circuit circuit = new Circuit();
        try {
            circuit.readFile();
        } catch (FileNotFoundException e) {
            System.out.println("Data file doesn't exist!");
        }
        //System.out.println(circuit.checkIfConnected());
        /*circuit.gatherUnions();
        for (int k = 0; k < circuit.numberOfUnions; k++) {
            System.out.println(circuit.unionArray[k].unionNumber + " " + circuit.unionArray[k].non);
        }*/
        /*for(int i=0;i<=circuit.numberOfNodes;i++){
            //if(circuit.nodeArray[i].parentNode!=circuit.nodeArray[i].nodeNumber) System.out.println(circuit.nodeArray[i].parentElement);
            System.out.println(circuit.nodeArray[i].parent_element);
        }*/
        circuit.gatherUnions();
        /*for (int k = 0; k < circuit.numberOfUnions; k++) {
            System.out.print(k+":");
            for(int i =0;i<circuit.unionArray[k].non;i++) System.out.print(circuit.unionArray[k].parent_node+" ");
            System.out.println();
        }*/
        for (int k = 0; k <= 1000; k++) {
            circuit.updateUnions();
            circuit.updateBranches();

            if (k % 100 == 0)
                circuit.printData();
        }

        //Phase two
        //JFrame Main = new MainPanel(circuit);
    }
}
