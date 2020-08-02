import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

class Node {
    int nodeNumber;
    float previousVoltage = 0;
    float previousVoltage_t = 0;
    float voltage = 0;
    ArrayList<Float> voltage_t = new ArrayList<Float>();
    float previousCurrent = 0;
    float current = 0;
    float newCurrent = 0;

    //for unions
    boolean needs_parent = false;
    int parentNode;
    int unionNumber;
    boolean _added = false;
    Set<Node> neighbors = new HashSet<Node>();
    Branch parentElement;
    int parent_element;

    //for voltage source current
    int numberOfVS;
    float expected_current = 0;

    //-2 error
    boolean toBeChecked = true;
    Set<Branch> neighborCurrentSources = new HashSet<Branch>();

    //-5 error
    boolean connected = false;
    boolean[] neighbor_nodes = new boolean[100];

    Node(int a) {
        nodeNumber = a;
        unionNumber = nodeNumber;
        parentNode = nodeNumber;
        voltage_t.add(0f);
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
    boolean independent = true;
    //independence type
    int type = 0;
    float resistance;
    float capacity;
    float inductance;

    float current = 0;
    float previousCurrent = 0;
    float previousCurrent_t = 0;
    float nextCurrent_plus = 0;
    float nextCurrent_negative = 0;
    ArrayList<Float> current_t = new ArrayList<Float>();

    float voltage;
    float previousVoltage;
    float previousVoltage_t;
    ArrayList<Float> voltage_t = new ArrayList<Float>();

    float power;
    ArrayList<Float> power_t = new ArrayList<Float>();

    //for KCL & KVL
    int type_of_source = 0;
    /* two types of sources:
       1. Current source
       2. Voltage source */

    public Branch(String name, int startNode, int endNode, float value) {
    }

    float getCurrent() {
        return current;
    }

    float getVoltage(Node a, Node b) {
        return a.voltage - b.voltage;
    }

    //for independent ones
    void updateBranch(Node startNode, Node endNode, float dt, float dv, float time) {
    }

    //dependent type 1 & 3
    void updateBranch(Node[] nodes, float dt, float dv) {
    }

    //dependent type 2
    void updateBranch(Branch[] branches, float dt, float dv) {
    }

    //dependent type 4
    void updateBranch(Branch[] branches, Node[] nodes, float dt, float dv) {
    }

    //for each step
    void updateBranchFinal(Node startNode, Node endNode, float dt, float dv, float time, int step) {
    }
}

class Circuit {
    String filePath = "Input.txt";
    int numberOfNodes = 0;
    int numberOfBranches = 0;
    int numberOfUnions = 0;
    float dt, dv, di, duration, time = 0;
    int step = 0;
    int[][] adjMatrix = new int[100][100];
    Node[] nodeArray = new Node[100];
    Branch[] branchArray = new Branch[100];
    Union[] unionArray = new Union[50];

    Circuit() {
        for (int i = 0; i < 100; i++) for (int j = 0; j < 100; j++) adjMatrix[i][j] = 0;
    }

    Circuit(String filePath) {
        for (int i = 0; i < 100; i++) for (int j = 0; j < 100; j++) adjMatrix[i][j] = 0;
        this.filePath = filePath;
    }

    class Union {
        Node[] nodes = new Node[10];
        int unionNumber;
        int parent_node;
        int non = 0;

        //for solution
        float totalCurrent1 = 0;
        float current = 0;
        float totalCurrent2 = 0;


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
            for (int k = 0; nodes[k] != null; k++) {
                if (k == 0 && parent_node == 0) {
                    nodes[0].voltage = 0;
                    nodes[0].previousVoltage = 0;
                } else if (k == 0) {
                    nodes[0].previousVoltage = nodes[0].voltage;
                    nodes[0].voltage += dv * (Math.abs(totalCurrent1) - Math.abs(totalCurrent2)) / di;
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

        void updateVoltagesFinal() {
            for (int k = 0; nodes[k] != null; k++) {
                if (k == 0 && parent_node == 0) {
                    nodes[0].voltage_t.add(0f);
                    nodes[0].previousVoltage_t = nodes[0].voltage_t.get(step);
                } else if (k == 0) {
                    nodes[0].voltage_t.add(nodes[0].voltage);
                    nodes[0].previousVoltage_t = nodes[0].voltage_t.get(step + 1);
                } else {
                    if (nodes[k].parentNode == nodes[k].parentElement.port1) {
                        nodes[k].voltage_t.add(nodeArray[nodes[k].parentNode].voltage - nodes[k].parentElement.voltage);
                        nodes[k].previousVoltage = nodes[k].voltage_t.get(step + 1);
                    } else if (nodes[k].parentNode == nodes[k].parentElement.port2) {
                        nodes[k].voltage_t.add(nodeArray[nodes[k].parentNode].voltage + nodes[k].parentElement.voltage);
                        nodes[k].previousVoltage = nodes[k].voltage_t.get(step + 1);
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

    void updateCircuit() throws IOException {
        gatherUnions();
        File output = new File("Output.txt");
        FileWriter fileWriter = new FileWriter(output);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        int stepSize = (int) (duration / dt) / 100;

        if (check_error5()) {
            //updateBranches();
            for (time = 0; time <= duration; time += dt) {
                for (int cnt = 0; cnt < 50000; cnt++) {
                    boolean flag = false;
                    updateBranches();
                    //updateNodes();
                    updateUnions();
                    for (int k = 0; k < numberOfUnions; k++) {
                        System.out.println(unionArray[k].current + " cnt:" + cnt + " step:" + step);
                        if (unionArray[k].current > di) flag = true;
                    }
                    if (!flag) break;
                }

                updateUnionsFinal();
                updateBranchesFinal();
                if (step > 5 && !check_error2()) {
                    bufferedWriter.write("Error 2 occurred.\n");
                    break;
                }
                if (stepSize == 0 || step % stepSize == 0) {
                    //printData(bufferedWriter);
                    printDataFinal(bufferedWriter);
                    //System.out.println(stepSize+" "+step);
                }
                step++;
            }
        } else bufferedWriter.write("The circuit is not valid; error -5.\n");
        bufferedWriter.close();
        fileWriter.close();
    }

    void updateBranches() {
        setExpectedCurrents();
        for (int j = 0; branchArray[j] != null; j++) {
            if (branchArray[j].independent)
                branchArray[j].updateBranch(nodeArray[branchArray[j].port1], nodeArray[branchArray[j].port2], dt, dv, time);
            else if (branchArray[j].type == 1)
                branchArray[j].updateBranch(nodeArray, dt, dv);
            else if (branchArray[j].type == 2)
                branchArray[j].updateBranch(branchArray, dt, dv);
            else if (branchArray[j].type == 3)
                branchArray[j].updateBranch(nodeArray, dt, dv);
            else if (branchArray[j].type == 4)
                branchArray[j].updateBranch(branchArray, nodeArray, dt, dv);
        }
    }

    void updateBranchesFinal() {
        for (int j = 0; branchArray[j] != null; j++) {
            //if (branchArray[j].independence)
            branchArray[j].updateBranchFinal(nodeArray[branchArray[j].port1], nodeArray[branchArray[j].port2], dt, dv, time, step);
            /*else if (branchArray[j].type == 1)
                branchArray[j].updateBranchFinal(nodeArray, dt, dv, step);
            else if (branchArray[j].type == 2)
                branchArray[j].updateBranchFinal(branchArray, dt, dv);*/
        }
    }

    void updateUnions() {
        for (int k = 0; unionArray[k] != null; k++) {
            unionArray[k].totalCurrent1 = 0;
            unionArray[k].totalCurrent2 = 0;
            for (int i = 0; unionArray[k].nodes[i] != null; i++) {
                for (int j = 0; branchArray[j] != null; j++) {
                    if (branchArray[j].port1 == unionArray[k].nodes[i].nodeNumber && branchArray[j].type_of_source != 2) {
                        unionArray[k].totalCurrent1 += branchArray[j].current;
                        //System.out.println("Element: " + branchArray[j].name + " current to be add: " + branchArray[j].current);
                        //unionArray[k].nextCurrent += branchArray[j].previousCurrent;
                        unionArray[k].totalCurrent2 += branchArray[j].nextCurrent_plus;
                    } else if (branchArray[j].port2 == unionArray[k].nodes[i].nodeNumber && branchArray[j].type_of_source != 2) {
                        unionArray[k].totalCurrent1 -= branchArray[j].current;
                        //System.out.println("Element: " + branchArray[j].name + " current to be add: " + branchArray[j].current);
                        //unionArray[k].nextCurrent -= branchArray[j].previousCurrent;
                        unionArray[k].totalCurrent2 -= branchArray[j].nextCurrent_negative;
                    }
                }
            }
            unionArray[k].current = unionArray[k].totalCurrent1;
        }
        for (int k = 0; k < numberOfUnions; k++) unionArray[k].updateVoltages();
    }

    void updateUnionsFinal() {
        for (int k = 0; k < numberOfUnions; k++)
            unionArray[k].updateVoltagesFinal();
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
                    System.out.println("pCurrent: " + branchArray[j].previousCurrent + " nCurrent_plus:" + branchArray[j].nextCurrent_plus);
                    //nodeArray[i].previousCurrent += branchArray[j].nextCurrent_plus;
                } else if (branchArray[j].port2 == cnt) {
                    nodeArray[i].newCurrent -= branchArray[j].current;
                    nodeArray[i].previousCurrent -= branchArray[j].previousCurrent;
                    System.out.println("pCurrent: " + branchArray[j].previousCurrent + " nCurrent_neg:" + branchArray[j].nextCurrent_negative);
                    //nodeArray[i].previousCurrent -= branchArray[j].nextCurrent_negative;
                }
            }
            cnt++;
        }
        for (int i = 0; nodeArray[i] != null; i++) {
            nodeArray[i].previousVoltage = nodeArray[i].voltage;
            nodeArray[i].voltage += dv * (Math.abs(nodeArray[i].newCurrent) - Math.abs(nodeArray[i].previousCurrent)) / di;
            nodeArray[i].current = (nodeArray[i].newCurrent + nodeArray[i].previousCurrent) / 2;
            /*if(nodeArray[i].newCurrent < 0.5);
            else if (nodeArray[i].newCurrent > nodeArray[i].previousCurrent)
                nodeArray[i].voltage += dv;
            else if (nodeArray[i].newCurrent < nodeArray[i].previousCurrent)
                nodeArray[i].voltage -= dv;*/
            if (i == 0) {
                nodeArray[i].voltage = 0;
                nodeArray[i].previousVoltage = 0;
            }
        }
    }

    void printData(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write("---------- Time:" + time + " ----------\n");
        for (int i = 0; nodeArray[i] != null; i++) {
            //System.out.println("Node: " + nodeArray[i].nodeNumber + " voltage:" + nodeArray[i].voltage);
            bufferedWriter.write("Node: " + nodeArray[i].nodeNumber + " voltage:" + nodeArray[i].voltage + " previousVoltage:" + nodeArray[i].previousVoltage + "\n");
        }

        //System.out.println();
        bufferedWriter.write("\n");
        for (int k = 0; k < numberOfUnions; k++) {
            //System.out.println("Union: " + unionArray[k].unionNumber + " current:" + unionArray[k].current);
            bufferedWriter.write("Union: " + unionArray[k].unionNumber + " current:" + unionArray[k].current + " totalCurrent1:" + unionArray[k].totalCurrent1 + " totalCurrent2:" + unionArray[k].totalCurrent2 + "\n");
        }
        //System.out.println();
        bufferedWriter.write("\n");
        for (int j = 0; branchArray[j] != null; j++) {
            //System.out.println("Branch: " + branchArray[j].name + " voltage:" + branchArray[j].getVoltage(nodeArray[branchArray[j].port1], nodeArray[branchArray[j].port2]) + " current:" + branchArray[j].getCurrent());
            bufferedWriter.write("Branch: " + branchArray[j].name + " voltage:" + branchArray[j].getVoltage(nodeArray[branchArray[j].port1], nodeArray[branchArray[j].port2]) + " current:" + branchArray[j].getCurrent() + "\n");
        }
        //System.out.println("----------");
    }

    void printDataFinal(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter.write("---------- Time:" + time + ", step:" + step + " ----------\n");
        for (int i = 0; nodeArray[i] != null; i++) {
            //System.out.println("Node: " + nodeArray[i].nodeNumber + " voltage:" + nodeArray[i].voltage);
            bufferedWriter.write("Node: " + nodeArray[i].nodeNumber + " voltage:" + String.format("%.2f", nodeArray[i].voltage_t.get(step + 1)) + "\n");
        }

        //System.out.println();
        bufferedWriter.write("\n");
        for (int k = 0; k < numberOfUnions; k++) {
            //System.out.println("Union: " + unionArray[k].unionNumber + " current:" + unionArray[k].current);
            bufferedWriter.write("Union: " + unionArray[k].unionNumber + " current:" + String.format("%.1f", unionArray[k].current) + "\n");
        }
        //System.out.println();
        bufferedWriter.write("\n");
        for (int j = 0; branchArray[j] != null; j++) {
            //System.out.println("Branch: " + branchArray[j].name + " voltage:" + branchArray[j].getVoltage(nodeArray[branchArray[j].port1], nodeArray[branchArray[j].port2]) + " current:" + branchArray[j].getCurrent());
            bufferedWriter.write("Branch: " + branchArray[j].name + " voltage:" + String.format("%.2f", branchArray[j].voltage_t.get(step)) + " current:" + String.format("%.2f", branchArray[j].current_t.get(step)) + "\n");
        }
        //System.out.println("----------");
    }

    float aFloat(String str) {
        float result = 0;
        Pattern num = Pattern.compile("[\\.\\d]+");
        Matcher matcher = num.matcher(str);
        matcher.find();
        if (str.matches("[\\.\\d]+G")) result = (float) (1E9 * Float.parseFloat(matcher.group()));
        if (str.matches("[\\.\\d]+M")) result = (float) (1E6 * Float.parseFloat(matcher.group()));
        if (str.matches("[\\.\\d]+k")) result = (float) (1E3 * Float.parseFloat(matcher.group()));
        if (str.matches("[\\.\\d]+")) result = Float.parseFloat(matcher.group());
        if (str.matches("[\\.\\d]+m")) result = (float) (1E-3 * Float.parseFloat(matcher.group()));
        if (str.matches("[\\.\\d]+u")) result = (float) (1E-6 * Float.parseFloat(matcher.group()));
        if (str.matches("[\\.\\d]+n")) result = (float) (1E-9 * Float.parseFloat(matcher.group()));
        return result;
    }

    void readFile() throws FileNotFoundException {
        File file = new File(filePath);
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
                } else if (element_name.matches("E(\\d)+")) {
                    int startNode = Integer.parseInt(info[1]);
                    int endNode = Integer.parseInt(info[2]);

                    int related_port1 = Integer.parseInt(info[3]);
                    int related_port2 = Integer.parseInt(info[4]);
                    float gain = aFloat(info[5]);
                    VoltageDependentVS voltageDependentVS = new VoltageDependentVS(element_name, startNode, endNode, related_port1, related_port2, gain);
                    addElement(voltageDependentVS);
                } else if (element_name.matches("H(\\d)+")) {
                    int startNode = Integer.parseInt(info[1]);
                    int endNode = Integer.parseInt(info[2]);

                    String dependentElementName = info[3];
                    float gain = aFloat(info[4]);
                    CurrentDependentVS currentDependentVS = new CurrentDependentVS(element_name, startNode, endNode, dependentElementName, gain);
                    addElement(currentDependentVS);
                } else if (element_name.matches("dv")) dv = aFloat(info[1]);
                else if (element_name.matches("dt")) dt = aFloat(info[1]);
                else if (element_name.matches("di")) di = aFloat(info[1]);
                else if (element_name.matches("\\.tran")) duration = aFloat(info[1]);
                line = br.readLine();
            }
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
            if (branchArray[j].type_of_source == 2) {
                start = branchArray[j].port1;
                end = branchArray[j].port2;
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
            if (branchArray[j].type_of_source == 2) {
                start = branchArray[j].port1;
                end = branchArray[j].port2;
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
        for (int k = 0; k < numberOfUnions; k++) {
            unionArray[k].updateVoltages();
        }
    }

    boolean first_time = true;

    void setExpectedCurrents() {
        for (int j = 0; j < numberOfBranches; j++)
            if (branchArray[j].type_of_source == 2 && first_time) {
                int start = branchArray[j].port1;
                int end = branchArray[j].port2;
                nodeArray[start].numberOfVS++;
                nodeArray[end].numberOfVS++;
            }
        first_time = false;
        for (int i = 0; i <= numberOfNodes; i++)
            if (nodeArray[i].numberOfVS == 1) {
                nodeArray[i].expected_current = 0;
                for (int j = 0; j < numberOfBranches; j++) {
                    if (branchArray[j].port1 == i && branchArray[j].type_of_source != 2)
                        nodeArray[i].expected_current += branchArray[j].current;
                    else if (branchArray[j].port2 == i && branchArray[j].type_of_source != 2)
                        nodeArray[i].expected_current -= branchArray[j].current;
                }
            }
    }

    boolean check_error2() {
        boolean flag = true;
        prepareNodesToBeChecked();
        for (int i = 0; i <= numberOfNodes; i++) {
            if (nodeArray[i].toBeChecked) {
                float sum = 0;
                for (Branch cs : nodeArray[i].neighborCurrentSources) {
                    if (nodeArray[i].nodeNumber == cs.port1) sum += cs.current;
                    else sum -= cs.current;
                }
                flag = sum <= di;
            }
        }
        return flag;
    }

    void prepareNodesToBeChecked() {
        for (int j = 0; j < numberOfBranches; j++) {
            int port1 = branchArray[j].port1;
            int port2 = branchArray[j].port2;
            if (branchArray[j].type_of_source == 1) {
                nodeArray[port1].neighborCurrentSources.add(branchArray[j]);
                nodeArray[port2].neighborCurrentSources.add(branchArray[j]);
            } else {
                nodeArray[port1].toBeChecked = false;
                nodeArray[port2].toBeChecked = false;
            }
        }
    }

    boolean check_error3() {
        boolean result = true;
        for (int j = 0; j < numberOfBranches; j++)
            if (branchArray[j].type_of_source == 2)
                for (int t = j + 1; t < numberOfBranches; t++)
                    if (branchArray[t].type_of_source == 2) {
                        int s1 = branchArray[j].port1;
                        int e1 = branchArray[j].port2;
                        int s2 = branchArray[t].port2;
                        int e2 = branchArray[t].port2;
                        if (s1 == s2 && e1 == e2)
                            result = branchArray[j].voltage == branchArray[t].voltage;
                        else if (s1 == e2 && s2 == e1)
                            result = branchArray[j].voltage == -branchArray[t].voltage;
                    }
        return result;
    }

    boolean check_error5() {
        boolean flag = true;
        nodeArray[0].connected = true;
        set_connections(0);
        for (int i = 0; i <= numberOfNodes; i++) if (!nodeArray[i].connected) flag = false;
        return flag;
    }

    void set_connections(int node_number) {
        for (int j = 0; j < numberOfBranches; j++)
            if (adjMatrix[j][node_number] == 1 || adjMatrix[j][node_number] == -1)
                for (int i = 0; i <= numberOfNodes; i++)
                    if (adjMatrix[j][node_number] * adjMatrix[j][i] == -1 && !nodeArray[i].connected) {
                        nodeArray[i].connected = true;
                        set_connections(i);
                    }
        for (int i = 0; i <= numberOfNodes; i++) {
            int number_of_neighbors = 0;
            for (int j = 0; j < numberOfBranches; j++)
                if (adjMatrix[j][i] == 1 || adjMatrix[j][i] == -1) number_of_neighbors++;
            if (number_of_neighbors <= 1) nodeArray[i].connected = false;
        }
    }

    //charts
    void openCharts(String name) {
        int index = 0;
        for (int j = 0; j < numberOfBranches; j++)
            if (branchArray[j].name.equals(name)) index = j;

        ArrayList<Float> list1 = branchArray[index].voltage_t;
        ChartDrawer m1 = new ChartDrawer(1, list1, duration);
        JFrame f1 = new JFrame("Voltage");
        f1.add(m1);
        f1.setSize(940, 720);
        f1.setLocation(540, 150);
        f1.setVisible(true);

        ArrayList<Float> list = branchArray[index].current_t;
        ChartDrawer m = new ChartDrawer(2, list, duration);
        JFrame f = new JFrame("Current");
        f.add(m);
        f.setSize(940, 720);
        f.setLocation(560, 180);
        f.setVisible(true);

        ArrayList<Float> list2 = branchArray[index].power_t;
        ChartDrawer m2 = new ChartDrawer(3, list2, duration);
        JFrame f2 = new JFrame("Power");
        f2.add(m2);
        f2.setSize(940, 720);
        f2.setLocation(580, 210);
        f2.setVisible(true);
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
        for (int i = 1; i < size; i++) {
            float y = stepY * list.get(i) + startY;
            graphics.drawLine((int) xp, (int) yp, (int) (xp + stepX), (int) y);
            yp = y;
            xp += stepX;
        }
    }
}

public class Main {
    public static void main(String[] args) throws IOException {
        Circuit circuit = new Circuit();
        try {
            circuit.readFile();
        } catch (FileNotFoundException e) {
            System.out.println("Data file doesn't exist!");
        }

        circuit.updateCircuit();
        //circuit.gatherUnions();

        //Phase two
        /* MainPanel main = new MainPanel(circuit);
        main.setVisible(true);*/

        // goes to chart_button's actionListener
        JFrame questionFrame = new JFrame("Input");
        String name = JOptionPane.showInputDialog(questionFrame, "Enter the name of your element");
        if (name != null) circuit.openCharts(name);
    }
}

//Phase two

class ResistorPanel extends JPanel {
    int node1x, node1y, node2x, node2y;

    ResistorPanel(int node1, int node2, String name) {
        node1x = ((node1 - 1) % 6) * 100 + 50;
        node1y = 450 - (node1 - 1) / 6 * 100;
        node2x = ((node2 - 1) % 6) * 100 + 50;
        node2y = 450 - (node2 - 1) / 6 * 100;
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
        node1x = ((node1 - 1) % 6) * 100 + 50;
        node1y = 450 - (node1 - 1) / 6 * 100;
        node2x = ((node2 - 1) % 6) * 100 + 50;
        node2y = 450 - (node2 - 1) / 6 * 100;
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
        node1x = ((node1 - 1) % 6) * 100 + 50;
        node1y = 450 - (node1 - 1) / 6 * 100;
        node2x = ((node2 - 1) % 6) * 100 + 50;
        node2y = 450 - (node2 - 1) / 6 * 100;
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
        node1x = ((node1 - 1) % 6) * 100 + 50;
        node1y = 450 - (node1 - 1) / 6 * 100;
        node2x = ((node2 - 1) % 6) * 100 + 50;
        node2y = 450 - (node2 - 1) / 6 * 100;
        setBounds((node1x + node2x) / 2 - 50, (node1y + node2y) / 2 - 55, 100, 105);
        Color color = new Color(1, 1, 1, 1);
        setBackground(color);
        BufferedImage VoltageDCImage;
        JLabel VoltageDCLabel, NameLabel;
        if (node1x == node2x && node1y > node2y) {
            try {
                VoltageDCImage = ImageIO.read(new File("icons/Up side VoltageDC.png"));
                VoltageDCLabel = new JLabel(new ImageIcon(VoltageDCImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(20, 40, 50, 15);
                VoltageDCLabel.add(NameLabel);
                add(VoltageDCLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1x == node2x && node2y > node1y) {
            try {
                VoltageDCImage = ImageIO.read(new File("icons/Down side VoltageDC.png"));
                VoltageDCLabel = new JLabel(new ImageIcon(VoltageDCImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(20, 40, 50, 15);
                VoltageDCLabel.add(NameLabel);
                add(VoltageDCLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1y == node2y && node1x < node2x) {
            try {
                VoltageDCImage = ImageIO.read(new File("icons/Right side VoltageDC.png"));
                VoltageDCLabel = new JLabel(new ImageIcon(VoltageDCImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(45, 60, 50, 15);
                VoltageDCLabel.add(NameLabel);
                add(VoltageDCLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1y == node2y && node1x > node2x) {
            try {
                VoltageDCImage = ImageIO.read(new File("icons/Left side VoltageDC.png"));
                VoltageDCLabel = new JLabel(new ImageIcon(VoltageDCImage));
                NameLabel = new JLabel(name);
                NameLabel.setBackground(color);
                NameLabel.setBounds(45, 60, 50, 15);
                VoltageDCLabel.add(NameLabel);
                add(VoltageDCLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class VoltageACPanel extends JPanel {
    int node1x, node1y, node2x, node2y;

    VoltageACPanel(int node1, int node2) {
        node1x = ((node1 - 1) % 6) * 100 + 50;
        node1y = 450 - (node1 - 1) / 6 * 100;
        node2x = ((node2 - 1) % 6) * 100 + 50;
        node2y = 450 - (node2 - 1) / 6 * 100;
        setBounds((node1x + node2x) / 2 - 50, (node1y + node2y) / 2 - 55, 100, 105);
        Color color = new Color(1, 1, 1, 1);
        setBackground(color);
        BufferedImage VoltageACImage;
        JLabel VoltageACLabel;
        if (node1x == node2x && node1y > node2y) {
            try {
                VoltageACImage = ImageIO.read(new File("icons/Up side VoltageAC.png"));
                VoltageACLabel = new JLabel(new ImageIcon(VoltageACImage));
                add(VoltageACLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1x == node2x && node2y > node1y) {
            try {
                VoltageACImage = ImageIO.read(new File("icons/Down side VoltageAC.png"));
                VoltageACLabel = new JLabel(new ImageIcon(VoltageACImage));
                add(VoltageACLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1y == node2y && node1x < node2x) {
            try {
                VoltageACImage = ImageIO.read(new File("icons/Right side VoltageAC.png"));
                VoltageACLabel = new JLabel(new ImageIcon(VoltageACImage));
                add(VoltageACLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1y == node2y && node1x > node2x) {
            try {
                VoltageACImage = ImageIO.read(new File("icons/Left side VoltageAC.png"));
                VoltageACLabel = new JLabel(new ImageIcon(VoltageACImage));
                add(VoltageACLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class CurrentDCPanel extends JPanel {
    int node1x, node1y, node2x, node2y;

    CurrentDCPanel(int node1, int node2, String name) {
        node1x = ((node1 - 1) % 6) * 100 + 50;
        node1y = 450 - (node1 - 1) / 6 * 100;
        node2x = ((node2 - 1) % 6) * 100 + 50;
        node2y = 450 - (node2 - 1) / 6 * 100;
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

class CurrentACPanel extends JPanel {
    int node1x, node1y, node2x, node2y;

    CurrentACPanel(int node1, int node2) {
        node1x = ((node1 - 1) % 6) * 100 + 50;
        node1y = 450 - (node1 - 1) / 6 * 100;
        node2x = ((node2 - 1) % 6) * 100 + 50;
        node2y = 450 - (node2 - 1) / 6 * 100;
        setBounds((node1x + node2x) / 2 - 50, (node1y + node2y) / 2 - 55, 100, 105);
        Color color = new Color(1, 1, 1, 1);
        setBackground(color);
        BufferedImage CurrentACImage;
        JLabel CurrentACLabel;
        if (node1x == node2x && node1y > node2y) {
            try {
                CurrentACImage = ImageIO.read(new File("icons/Up side CurrentAC.png"));
                CurrentACLabel = new JLabel(new ImageIcon(CurrentACImage));
                add(CurrentACLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1x == node2x && node2y > node1y) {
            try {
                CurrentACImage = ImageIO.read(new File("icons/Down side CurrentAC.png"));
                CurrentACLabel = new JLabel(new ImageIcon(CurrentACImage));
                add(CurrentACLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1y == node2y && node1x < node2x) {
            try {
                CurrentACImage = ImageIO.read(new File("icons/Right side CurrentAC.png"));
                CurrentACLabel = new JLabel(new ImageIcon(CurrentACImage));
                add(CurrentACLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (node1y == node2y && node1x > node2x) {
            try {
                CurrentACImage = ImageIO.read(new File("icons/Left side CurrentAC.png"));
                CurrentACLabel = new JLabel(new ImageIcon(CurrentACImage));
                add(CurrentACLabel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class DependentCurrentPanel extends JPanel {
    int node1x, node1y, node2x, node2y;

    DependentCurrentPanel(int node1, int node2, String name) {
        node1x = ((node1 - 1) % 6) * 100 + 50;
        node1y = 450 - (node1 - 1) / 6 * 100;
        node2x = ((node2 - 1) % 6) * 100 + 50;
        node2y = 450 - (node2 - 1) / 6 * 100;
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
        } else if (node1y == node2y && node1x < node2x) {
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
        } else if (node1y == node2y && node1x > node2x) {
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
        node1x = ((node1 - 1) % 6) * 100 + 50;
        node1y = 450 - (node1 - 1) / 6 * 100;
        node2x = ((node2 - 1) % 6) * 100 + 50;
        node2y = 450 - (node2 - 1) / 6 * 100;
        setBounds((node1x + node2x) / 2 - 50, (node1y + node2y) / 2 - 55, 100, 105);
        Color color = new Color(1, 1, 1, 1);
        setBackground(color);
        BufferedImage DependentVoltageImage;
        JLabel DependentVoltageLabel, NameLabel;
        if (node1x == node2x && node1y > node2y) {
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
        } else if (node1x == node2x && node2y > node1y) {
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
        } else if (node1y == node2y && node1x < node2x) {
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
        } else if (node1y == node2y && node1x > node2x) {
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
        }
    }
}

class GNDPanel extends JPanel {
    int nodex, nodey;

    GNDPanel(int node) {
        nodex = ((node - 1) % 6) * 100 + 50;
        nodey = 450 - (node - 1) / 6 * 100;
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
        node1x = ((node1 - 1) % 6) * 100 + 50;
        node1y = 450 - (node1 - 1) / 6 * 100;
        node2x = ((node2 - 1) % 6) * 100 + 50;
        node2y = 450 - (node2 - 1) / 6 * 100;
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

    Element(JComponent frame, int[][] locationMatrix, char type, Branch branch, int node1, int node2) {
        dx = (node2 - 1) % 6 - (node1 - 1) % 6;
        dy = (node1 - 1) / 6 - (node2 - 1) / 6;
        n1i = (node1 - 1) % 6;
        n1j = 4 - (node1 - 1) / 6;
        n2i = (node2 - 1) % 6;
        n2j = 4 - (node2 - 1) / 6;
        if (node1 != 0 && node2 != 0) {
            locationMatrix[n1i][n1j] = 1;
            locationMatrix[n2i][n2j] = 1;
        }
        Draw(frame, locationMatrix, type, branch, node1, node2, dx, dy);
    }

    void Draw(JComponent frame, int[][] locationMatrix, char type, Branch branch, int node1, int node2, int dx, int dy) {
        int n1i, n1j;
        n1i = (node1 - 1) % 6;
        n1j = 4 - (node1 - 1) / 6;
        n2i = (node2 - 1) % 6;
        n2j = 4 - (node2 - 1) / 6;
        if (node1 != 0 && node2 != 0) {
            locationMatrix[n1i][n1j] = 1;
            locationMatrix[n2i][n2j] = 1;
        }
        if (node1 == 0) {
            if (n2i + 1 <= 5 && locationMatrix[n2i + 1][n2j] == 0)
                node1 = node2 + 1;
            else if (n2i - 1 >= 0 && locationMatrix[n2i - 1][n2j] == 0)
                node1 = node2 - 1;
            else if (n2j + 1 <= 4 && locationMatrix[n2i][n2j + 1] == 0)
                node1 = node2 - 6;
            else if (n2j - 1 >= 0 && locationMatrix[n2i][n2j - 1] == 0)
                node1 = node2 + 6;
            GNDPanel g = new GNDPanel(node1);
            frame.add(g);
            Draw(frame, locationMatrix, type, branch, node1, node2, dx, dy);
        } else if (node2 == 0) {
            if (n1i + 1 <= 5 && locationMatrix[n1i + 1][n1j] == 0)
                node2 = node1 + 1;
            else if (n1i - 1 >= 0 && locationMatrix[n1i - 1][n1j] == 0)
                node2 = node1 - 1;
            else if (n1j + 1 <= 4 && locationMatrix[n1i][n1j + 1] == 0)
                node2 = node1 - 6;
            else if (n1j - 1 >= 0 && locationMatrix[n1i][n1j - 1] == 0)
                node2 = node1 + 6;
            GNDPanel g = new GNDPanel(node2);
            frame.add(g);
            Draw(frame, locationMatrix, type, branch, node1, node2, dx, dy);
        } else if ((Math.abs(n1i - n2i) == 1 && n1j == n2j) || (Math.abs(n1j - n2j) == 1 && n1i == n2i)) {
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
                DependentCurrentPanel h = new DependentCurrentPanel(node1, node2, branch.name);
                frame.add(h);
            } else if (type == 'E') {
                DependentCurrentPanel e = new DependentCurrentPanel(node1, node2, branch.name);
                frame.add(e);
            } else if (type == 'G') {
                DependentVoltagePanel g = new DependentVoltagePanel(node1, node2, branch.name);
                frame.add(g);
            } else if (type == 'F') {
                DependentVoltagePanel f = new DependentVoltagePanel(node1, node2, branch.name);
                frame.add(f);
            }
        } else {
            if (dx == 0) {
                if (dy != 0 && locationMatrix[n1i][n1j + (dy / Math.abs(dy))] == 0) {
                    WirePanel w = new WirePanel(node1, node1 - 6 * dy / Math.abs(dy));
                    frame.add(w);
                    locationMatrix[n1i][n1j + (dy / Math.abs(dy))] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 - 6 * dy / Math.abs(dy), node2, dx, (dy / Math.abs(dy)) * (Math.abs(dy) - 1));
                } else if (locationMatrix[n1i + 1][n1j] == 0 && n1i + 1 <= 5) {
                    WirePanel w = new WirePanel(node1, node1 + 1);
                    frame.add(w);
                    locationMatrix[n1i + 1][n1j] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 + 1, node2, dx - 1, dy);
                } else if (n1i - 1 >= 0 && locationMatrix[n1i - 1][n1j] == 0) {
                    WirePanel w = new WirePanel(node1, node1 - 1);
                    frame.add(w);
                    locationMatrix[n1i - 1][n1j] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 - 1, node2, dx + 1, dy);
                }
            } else if (dy == 0) {
                if (dx != 0 && locationMatrix[n1i + (dx / Math.abs(dx))][n1j] == 0) {
                    WirePanel w = new WirePanel(node1, node1 + dx / Math.abs(dx));
                    frame.add(w);
                    locationMatrix[n1i + (dx / Math.abs(dx))][n1j] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 + dx / Math.abs(dx), node2, (dx / Math.abs(dx)) * (Math.abs(dx) - 1), dy);
                } else if (n1j + 1 <= 4 && locationMatrix[n1i][n1j + 1] == 0) {
                    WirePanel w = new WirePanel(node1, node1 - 6);
                    frame.add(w);
                    locationMatrix[n1i][n1j + 1] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 - 6, node2, dx, dy - 1);
                } else if (n1j - 1 >= 0 && locationMatrix[n1i][n1j - 1] == 0) {
                    WirePanel w = new WirePanel(node1, node1 + 6);
                    frame.add(w);
                    locationMatrix[n1i][n1j - 1] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 + 6, node2, dx, dy + 1);
                }
            } else {
                if (dy != 0 && locationMatrix[n1i][n1j + (dy / Math.abs(dy))] == 0) {
                    WirePanel w = new WirePanel(node1, node1 - 6 * dy / Math.abs(dy));
                    frame.add(w);
                    locationMatrix[n1i][n1j + (dy / Math.abs(dy))] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 - 6 * dy / Math.abs(dy), node2, dx, (dy / Math.abs(dy)) * (Math.abs(dy) - 1));
                } else if (dx != 0 && locationMatrix[n1i + (dx / Math.abs(dx))][n1j] == 0) {
                    WirePanel w = new WirePanel(node1, node1 + dx / Math.abs(dx));
                    frame.add(w);
                    locationMatrix[n1i + (dx / Math.abs(dx))][n1j] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 + dx / Math.abs(dx), node2, (dx / Math.abs(dx)) * (Math.abs(dx) - 1), dy);
                } else if (n1i + 1 <= 5 && locationMatrix[n1i + 1][n1j] == 0) {
                    WirePanel w = new WirePanel(node1, node1 + 1);
                    frame.add(w);
                    locationMatrix[n1i + 1][n1j] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 + 1, node2, dx - 1, dy);
                } else if (n1i - 1 >= 0 && locationMatrix[n1i - 1][n1j] == 0) {
                    WirePanel w = new WirePanel(node1, node1 - 1);
                    frame.add(w);
                    locationMatrix[n1i - 1][n1j] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 - 1, node2, dx + 1, dy);
                } else if (n1j + 1 <= 4 && locationMatrix[n1i][n1j + 1] == 0) {
                    WirePanel w = new WirePanel(node1, node1 - 6);
                    frame.add(w);
                    locationMatrix[n1i][n1j + 1] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 - 6, node2, dx, dy - 1);
                } else if (n1j - 1 >= 0 && locationMatrix[n1i][n1j - 1] == 0) {
                    WirePanel w = new WirePanel(node1, node1 + 6);
                    frame.add(w);
                    locationMatrix[n1i][n1j - 1] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 + 6, node2, dx, dy + 1);
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

    DataPanel() {
        setBounds(25, 25, 350, 600);

        button_border = BorderFactory.createLineBorder(Color.BLACK, 1);
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
        int x_run_button = x_load_button + width_load_button + 50;
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
                //Circuit circuit = new Circuit(filePath);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}

class CircuitPanel extends JComponent {
    int startNode, endNode;
    int[][] locationMatrix = new int[6][5];
    JButton draw_button;

    CircuitPanel() {
        setBounds(800, 25, 700, 600);
        setBackground(Color.WHITE);
        Border border = BorderFactory.createLineBorder(Color.BLACK, 2);
        setBorder(border);
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++)
                locationMatrix[i][j] = 0;
        }
    }

    void DrawCircuit(Circuit circuit, JFrame frame) {
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
                new Element(this, locationMatrix, circuit.branchArray[i].name.charAt(0), circuit.branchArray[i], startNode, endNode);
                frame.add(this);
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
                new Element(this, locationMatrix, circuit.branchArray[i].name.charAt(0), circuit.branchArray[i], startNode, endNode);
                frame.add(this);
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        Graphics2D g2d = (Graphics2D) g;
    }
}

class ResultPanel extends JComponent implements ActionListener {
    JButton run_button, draw_button;
    JTextArea ResultArea;
    CircuitPanel circuitPanel;
    Circuit circuit;
    JFrame MainFrame;

    ResultPanel(JFrame frame, CircuitPanel cp, Circuit c) {
        circuit = c;
        circuitPanel = cp;
        MainFrame = frame;
        setBounds(400, 25, 350, 600);
        Border button_border = BorderFactory.createLineBorder(Color.BLACK, 1);
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
        try {
            Draw_ButtonImage = ImageIO.read(new File("icons/Draw Button.png"));
            Draw_ButtonLabel = new JLabel(new ImageIcon(Draw_ButtonImage));
            draw_button.add(Draw_ButtonLabel);
        } catch (IOException e) {
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
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == draw_button) {
            //circuitPanel.DrawCircuit(circuit, MainFrame);
        }
    }
}

class SharifPanel extends JPanel {
    Image SharifImage;
    JLabel SharifLabel;

    SharifPanel() {
        Color color = new Color(1, 1, 1, 1);
        setBounds(1400, 650, 150, 150);
        setBackground(color);
        try {
            SharifImage = ImageIO.read(new File("icons/Sharif Icon.png"));
            SharifLabel = new JLabel(new ImageIcon(SharifImage));
            add(SharifLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class MainPanel extends JFrame {
    MainPanel(Circuit circuit) {
        setSize(1550, 800);
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

        CircuitPanel c = new CircuitPanel();
        //c.DrawCircuit(circuit, this);
        add(c);

        ResultPanel r = new ResultPanel(this, c, circuit);
        add(r);

        SharifPanel s = new SharifPanel();
        add(s);
    }
}

