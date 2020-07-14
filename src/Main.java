import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.Scanner;
import java.util.regex.Pattern;

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
    float nextCurrent_plus = 0;
    float nextCurrent_negative = 0;
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

class Circuit {
    int numberOfNodes = 0;
    int numberOfBranches = 0;
    int numberOfUnions = 0;
    float dt, dv, di;
    int[][] adjMatrix = new int[100][100];
    Node[] nodeArray = new Node[100];
    Branch[] branchArray = new Branch[100];
    Union[] unionArray = new Union[50];

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
        float nextCurrent = 0;

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
                    nodes[0].voltage += dv * (Math.abs(newCurrent) - Math.abs(nextCurrent)) / di;
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
            unionArray[k].nextCurrent = 0;
            for (int i = 0; unionArray[k].nodes[i] != null; i++) {
                for (int j = 0; j < numberOfBranches; j++) {
                    if (branchArray[j].port1 == unionArray[k].nodes[i].nodeNumber && branchArray[j].getClass() != VoltageSource.class) {
                        unionArray[k].newCurrent += branchArray[j].current;
                        //unionArray[k].nextCurrent += branchArray[j].previousCurrent;
                        unionArray[k].nextCurrent += branchArray[j].nextCurrent_plus;
                    } else if (branchArray[j].port2 == unionArray[k].nodes[i].nodeNumber && branchArray[j].getClass() != VoltageSource.class) {
                        unionArray[k].newCurrent -= branchArray[j].current;
                        //unionArray[k].nextCurrent -= branchArray[j].previousCurrent;
                        unionArray[k].nextCurrent -= branchArray[j].nextCurrent_negative;
                    }
                }

            }
            unionArray[k].current = (unionArray[k].newCurrent + unionArray[k].nextCurrent) / 2;
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
        for (int j = 0; j < numberOfBranches; j++)
            if (adjMatrix[j][node_number] == 1 || adjMatrix[j][node_number] == -1)
                for (int i = 0; i <= numberOfNodes; i++)
                    if ((adjMatrix[j][i] == 1 || adjMatrix[j][i] == -1) && nodeArray[i].connected == false) {
                        nodeArray[i].connected = true;
                        set_connections(i);
                    }
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
        circuit.gatherUnions();
        for (int k = 0; k <= 10000; k++) {
            circuit.updateUnions();
            circuit.updateBranches();
            if (k % 100 == 0)
                circuit.printData();
        }
    }
}
