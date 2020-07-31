import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    //dependent type 2 & 4
    void updateBranch(Branch[] branches, float dt, float dv) {
    }

    //for each step
    void updateBranchFinal(Node startNode, Node endNode, float dt, float dv, float time, int step) {
    }
}

class Circuit {
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
                    for (int k = 0; k < numberOfUnions; k++)
                        if (unionArray[k].current > di) flag = true;
                    if (!flag) break;
                }
                updateUnionsFinal();
                updateBranchesFinal();
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
                branchArray[j].updateBranch(branchArray, dt, dv);
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
                    if (branchArray[j].port1 == unionArray[k].nodes[i].nodeNumber && branchArray[j].getClass() != VoltageSource.class) {
                        unionArray[k].totalCurrent1 += branchArray[j].current;
                        System.out.println("Element: " + branchArray[j].name + " current to be add: " + branchArray[j].current);
                        //unionArray[k].nextCurrent += branchArray[j].previousCurrent;
                        unionArray[k].totalCurrent2 += branchArray[j].nextCurrent_plus;
                    } else if (branchArray[j].port2 == unionArray[k].nodes[i].nodeNumber && branchArray[j].getClass() != VoltageSource.class) {
                        unionArray[k].totalCurrent1 -= branchArray[j].current;
                        System.out.println("Element: " + branchArray[j].name + " current to be add: " + branchArray[j].current);
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
            bufferedWriter.write("Node: " + nodeArray[i].nodeNumber + " voltage:" + nodeArray[i].voltage_t.get(step + 1) + " previousVoltage_t:" + nodeArray[i].previousVoltage_t + "\n");
        }

        //System.out.println();
        bufferedWriter.write("\n");
        for (int k = 0; k < numberOfUnions; k++) {
            //System.out.println("Union: " + unionArray[k].unionNumber + " current:" + unionArray[k].current);
            bufferedWriter.write("Union: " + unionArray[k].unionNumber + " current:" + unionArray[k].current + " totalCurrent1:" + unionArray[k].totalCurrent1 + "\n");
        }
        //System.out.println();
        bufferedWriter.write("\n");
        for (int j = 0; branchArray[j] != null; j++) {
            //System.out.println("Branch: " + branchArray[j].name + " voltage:" + branchArray[j].getVoltage(nodeArray[branchArray[j].port1], nodeArray[branchArray[j].port2]) + " current:" + branchArray[j].getCurrent());
            bufferedWriter.write("Branch: " + branchArray[j].name + " voltage:" + branchArray[j].voltage_t.get(step) + " current:" + branchArray[j].current_t.get(step) + "\n");
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
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        } catch (
                IOException e) {
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

    boolean check_error2() {
        boolean flag = true;
        prepareNodesToBeChecked();
        for (int i = 0; i <= numberOfNodes; i++) {
            float sum = 0;
            for (Branch cs : nodeArray[i].neighborCurrentSources) {
                if (nodeArray[i].nodeNumber == cs.port1) sum += cs.current;
                else sum -= cs.current;
            }
            flag = sum <= di;
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
}

public class  Main {
    public static void main(String[] args) throws IOException {
        Circuit circuit = new Circuit();
        try {
            circuit.readFile();
        } catch (FileNotFoundException e) {
            System.out.println("Data file doesn't exist!");
        }
        /*circuit.gatherUnions();
        for (int k = 0; circuit.unionArray[k] != null; k++) {
            System.out.print("Union: " + k + ",parent node: " + circuit.unionArray[k].parent_node);
            for (int i = 1; circuit.unionArray[k].nodes[i] != null; i++) {
                System.out.print(". node[" + circuit.unionArray[k].nodes[i].nodeNumber + "]'s parent element: " + circuit.unionArray[k].nodes[i].parent_element + " ");
            }
            System.out.println();
        }*/
        circuit.updateCircuit();
        //System.out.println(circuit.check_error2());
    }
}
