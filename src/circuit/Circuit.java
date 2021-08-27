package circuit;

import circuit.branch.*;
import graph.*;
import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Circuit {
    String filePath = "Input.txt";
    public int numberOfNodes = 0;
    public int numberOfBranches = 0;
    int numberOfUnions = 0;
    float dt = 0, dv = 0, di = 0, duration = 0, time = 0;
    int step = 0;
    public int[][] adjMatrix = new int[100][100];
    Node[] nodeArray = new Node[100];
    public Branch[] branchArray = new Branch[100];
    Union[] unionArray = new Union[50];

    Circuit() {
        for (int i = 0; i < 100; i++) for (int j = 0; j < 100; j++) adjMatrix[i][j] = 0;
    }

    public Circuit(String filePath) {
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

    public void updateCircuit() throws IOException {
        gatherUnions();
        File output = new File("Output.txt");
        FileWriter fileWriter = new FileWriter(output);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        int stepSize = (int) (duration / dt) / 100;

        if (check_error5()) {
            if (check_error3()) {
                if (check_error2()) {
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
                            bufferedWriter.write("\n>>Error 2 occurred.\n");
                            break;
                        }
                        if (stepSize == 0 || step % stepSize == 0) {
                            //printData(bufferedWriter);
                            printDataFinal(bufferedWriter);
                            //System.out.println(stepSize+" "+step);
                        }
                        step++;
                    }
                } else bufferedWriter.write("The circuit is not valid; error -2.\n");
            } else bufferedWriter.write("The circuit is not valid; error -3.\n");
        } else bufferedWriter.write("The circuit is not valid; error -5.\n");
        bufferedWriter.close();
        fileWriter.close();
        //openNodes();
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
        bufferedWriter.write("      ---------- Time:" + time + ", step:" + step + " ----------\n");
        for (int i = 0; nodeArray[i] != null; i++) {
            //System.out.println("Node: " + nodeArray[i].nodeNumber + " voltage:" + nodeArray[i].voltage);
            bufferedWriter.write("  Node: " + nodeArray[i].nodeNumber + " voltage:" + String.format("%.3f", nodeArray[i].voltage_t.get(step + 1)) + "\n");
        }

        //System.out.println();
        bufferedWriter.write("\n");
        for (int k = 0; k < numberOfUnions; k++) {
            //System.out.println("Union: " + unionArray[k].unionNumber + " current:" + unionArray[k].current);
            bufferedWriter.write("  Union: " + unionArray[k].unionNumber + " current:" + String.format("%.1f", unionArray[k].current) + "\n");
        }
        //System.out.println();
        bufferedWriter.write("\n");
        for (int j = 0; branchArray[j] != null; j++) {
            //System.out.println("Branch: " + branchArray[j].name + " voltage:" + branchArray[j].getVoltage(nodeArray[branchArray[j].port1], nodeArray[branchArray[j].port2]) + " current:" + branchArray[j].getCurrent());
            bufferedWriter.write("  Branch: " + branchArray[j].name + " voltage:" + String.format("%.3f", branchArray[j].voltage_t.get(step)) + "   current:" + String.format("%.3f", branchArray[j].current_t.get(step)) + "   power:" + String.format("%.3f", branchArray[j].power_t.get(step)) + "\n");
        }
        //System.out.println("----------");
    }

    int line_number = 0, size = 0;

    float aFloat(String str) {
        float result = 0;
        Pattern num = Pattern.compile("[\\.\\d]+");
        Matcher matcher = num.matcher(str);
        if (matcher.find()) {
            if (str.matches("[\\.\\d]+G")) result = (float) (1E9 * Float.parseFloat(matcher.group()));
            if (str.matches("[\\.\\d]+M")) result = (float) (1E6 * Float.parseFloat(matcher.group()));
            if (str.matches("[\\.\\d]+K")) result = (float) (1E3 * Float.parseFloat(matcher.group()));
            if (str.matches("[\\.\\d]+")) result = Float.parseFloat(matcher.group());
            if (str.matches("[\\.\\d]+m")) result = (float) (1E-3 * Float.parseFloat(matcher.group()));
            if (str.matches("[\\.\\d]+u")) result = (float) (1E-6 * Float.parseFloat(matcher.group()));
            if (str.matches("[\\.\\d]+n")) result = (float) (1E-9 * Float.parseFloat(matcher.group()));
        } else {
            System.out.println("Wrong input at line " + line_number + ", please try again.");
            System.exit(0);
        }
        if (str.matches("-(.)*")) {
            System.out.println("Wrong input at line " + line_number + ", please try again.");
            System.exit(0);
        }
        return result;
    }

    public void readFile() throws FileNotFoundException {
        File file = new File(filePath);
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            String line;
            String[] info = new String[10];

            line = br.readLine();
            while (line != null) {
                boolean flag = false;
                line_number++;
                info = line.split("\\s+");
                size = info.length;
                String element_name = new String(info[0]);
                if (element_name.matches("\\*(.)*")) flag = true;
                if (element_name.matches("R(\\d)+")) {
                    if (size == 4) {
                        flag = true;
                        int startNode = Integer.parseInt(info[1]);
                        int endNode = Integer.parseInt(info[2]);

                        float value = aFloat(info[3]);
                        Resistor resistor = new Resistor(element_name, startNode, endNode, value);
                        addElement(resistor);
                    } else {
                        System.out.println("Wrong input at line " + line_number + ", please try again.");
                        System.exit(0);
                    }
                }
                if (element_name.matches("L(\\d)+")) {
                    if (size == 4) {
                        flag = true;
                        int startNode = Integer.parseInt(info[1]);
                        int endNode = Integer.parseInt(info[2]);

                        float value = aFloat(info[3]);
                        Inductor inductor = new Inductor(element_name, startNode, endNode, value);
                        addElement(inductor);
                    } else {
                        System.out.println("Wrong input at line " + line_number + ", please try again.");
                        System.exit(0);
                    }
                }
                if (element_name.matches("C(\\d)+")) {
                    if (size == 4) {
                        flag = true;
                        int startNode = Integer.parseInt(info[1]);
                        int endNode = Integer.parseInt(info[2]);

                        float value = aFloat(info[3]);
                        Capacitor capacitor = new Capacitor(element_name, startNode, endNode, value);
                        addElement(capacitor);
                    } else {
                        System.out.println("Wrong input at line " + line_number + ", please try again.");
                        System.exit(0);
                    }
                }
                if (element_name.matches("I([a-zA-Z])*(\\d)?")) {
                    if (size == 7) {
                        flag = true;
                        int startNode = Integer.parseInt(info[1]);
                        int endNode = Integer.parseInt(info[2]);

                        float offset = aFloat(info[3]);
                        float amplitude = aFloat(info[4]);
                        float frequency = aFloat(info[5]);
                        float phase = Float.parseFloat(info[6]);
                        CurrentSource cs = new CurrentSource(element_name, startNode, endNode, offset, amplitude, frequency, phase);
                        addElement(cs);
                    } else {
                        System.out.println("Wrong input at line " + line_number + ", please try again.");
                        System.exit(0);
                    }
                }
                if (element_name.matches("V([a-zA-Z])*(\\d)?")) {
                    if (size == 7) {
                        flag = true;
                        int startNode = Integer.parseInt(info[1]);
                        int endNode = Integer.parseInt(info[2]);
                        if (startNode == endNode) {
                            System.out.println("Wrong input at line " + line_number + ", please try again.");
                            System.exit(0);
                        }
                        float offset = aFloat(info[3]);
                        float amplitude = aFloat(info[4]);
                        float frequency = aFloat(info[5]);
                        float phase = Float.parseFloat(info[6]);
                        VoltageSource vs = new VoltageSource(element_name, startNode, endNode, offset, amplitude, frequency, phase);
                        addElement(vs);
                    } else {
                        System.out.println("Wrong input at line " + line_number + ", please try again.");
                        System.exit(0);
                    }
                }
                if (element_name.matches("G(\\d)+")) {
                    if (size == 6) {
                        flag = true;
                        int startNode = Integer.parseInt(info[1]);
                        int endNode = Integer.parseInt(info[2]);

                        int related_port1 = Integer.parseInt(info[3]);
                        int related_port2 = Integer.parseInt(info[4]);
                        float gain = aFloat(info[5]);
                        VoltageDependentCS voltageDependentCS = new VoltageDependentCS(element_name, startNode, endNode, related_port1, related_port2, gain);
                        addElement(voltageDependentCS);
                    } else {
                        System.out.println("Wrong input at line " + line_number + ", please try again.");
                        System.exit(0);
                    }
                }
                if (element_name.matches("F(\\d)+")) {
                    flag = true;
                    int startNode = Integer.parseInt(info[1]);
                    int endNode = Integer.parseInt(info[2]);

                    String dependentElementName = info[3];
                    float gain = aFloat(info[4]);
                    CurrentDependentCS currentDependentCS = new CurrentDependentCS(element_name, startNode, endNode, dependentElementName, gain);
                    addElement(currentDependentCS);
                }
                if (element_name.matches("E(\\d)+")) {
                    flag = true;
                    int startNode = Integer.parseInt(info[1]);
                    int endNode = Integer.parseInt(info[2]);

                    int related_port1 = Integer.parseInt(info[3]);
                    int related_port2 = Integer.parseInt(info[4]);
                    float gain = aFloat(info[5]);
                    VoltageDependentVS voltageDependentVS = new VoltageDependentVS(element_name, startNode, endNode, related_port1, related_port2, gain);
                    addElement(voltageDependentVS);
                }
                if (element_name.matches("H(\\d)+")) {
                    if (size == 5) {
                        flag = true;
                        int startNode = Integer.parseInt(info[1]);
                        int endNode = Integer.parseInt(info[2]);

                        String dependentElementName = info[3];
                        float gain = aFloat(info[4]);
                        CurrentDependentVS currentDependentVS = new CurrentDependentVS(element_name, startNode, endNode, dependentElementName, gain);
                        addElement(currentDependentVS);
                    } else {
                        System.out.println("Wrong input at line " + line_number + ", please try again.");
                        System.exit(0);
                    }
                }
                if (element_name.matches("D(\\d)+")) {
                    flag = true;
                    int startNode = Integer.parseInt(info[1]);
                    int endNode = Integer.parseInt(info[2]);

                    float value = aFloat(info[3]);
                    Diode diode = new Diode(element_name, startNode, endNode, value);
                    addElement(diode);
                }
                if (element_name.matches("dv")) {
                    dv = aFloat(info[1]);
                    flag = true;
                }
                if (element_name.matches("dt")) {
                    flag = true;
                    dt = aFloat(info[1]);
                }
                if (element_name.matches("di")) {
                    flag = true;
                    di = aFloat(info[1]);
                }
                if (element_name.matches("\\.tran")) {
                    flag = true;
                    duration = aFloat(info[1]);
                }
                if (flag) line = br.readLine();
                else {
                    System.out.println("Wrong input at line " + line_number + ", please try again.");
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (dv == 0 || dt == 0 || di == 0 || duration == 0) {
            System.out.println("Wrong input, error -1.");
            System.exit(0);
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
                if (sum >= di) flag = false;
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
                        int s2 = branchArray[t].port1;
                        int e2 = branchArray[t].port2;
                        if (s1 == s2 && e1 == e2) {
                            if (branchArray[j].voltage != branchArray[t].voltage) result = false;
                        } else if (s1 == e2 && s2 == e1)
                            if (branchArray[j].voltage != -branchArray[t].voltage) result = false;
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
    public void openCharts(String name) {
        int index = 0;
        if (name.equals("nodes")) openNodes();
        else {
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

    void openNodes() {
        for (int i = 1; i <= numberOfNodes; i++) {
            ArrayList<Float> list1 = nodeArray[i].voltage_t;
            ChartDrawer m1 = new ChartDrawer(4, list1, duration);
            JFrame f1 = new JFrame("Voltage [" + i + "]");
            f1.add(m1);
            f1.setSize(940, 720);
            f1.setLocation(500 + i * 20, 120 + i * 30);
            f1.setVisible(true);
        }
    }
}
