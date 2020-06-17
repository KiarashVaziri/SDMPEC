import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;

abstract class Base {
    double getVoltage() {
        return 0;
    }
}

class Ground extends Base {
    static final double voltage = 0;
}

class Node extends Base {
    int nodeNumber;
    float previousVoltage = 0;
    float voltage = 0;
    float newVoltage = 0;
    float previousCurrent = 0;
    float current = 0;
    float newCurrent = 0;

    Node(int a) {
        nodeNumber = a;
    }

    double getVoltage() {
        return voltage;
    }
}

class Branch {
    String name;
    int port1, port2;
    float resistance;
    float capacity;
    float inductance;
    float current = 0;
    float previousCurrent = 0;
    float newCurrent = 0;
    float voltage;
    float power;

    public Branch(int startNode, int endNode, float value) {
    }

    float getCurrent() {
        return current;
    }

    float getVoltage(Node a, Node b) {
        return voltage;
    }

    void updateBranch(Node startNode, Node endNode, float dt, float dv) {
    }
}

class Resistor extends Branch {
    Resistor(int a, int b, float r) {
        super(a, b, r);
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
    }
}

class CurrentSource extends Branch {
    final float value;

    CurrentSource(int a, int b, float value) {
        super(a, b, value);
        port1 = a;
        port2 = b;
        //this.voltage = (float) (port1.getVoltage() - port2.getVoltage());
        this.current = value;
        this.value = value;
    }

    @Override
    float getVoltage(Node a, Node b) {
        return a.voltage - b.voltage;
    }

    @Override
    void updateBranch(Node a, Node b, float dt, float dv) {
        previousCurrent = current;
        current = value;
    }
}

class VoltageSource extends Branch {
    VoltageSource(int a, int b, float value) {
        super(a, b, value);
        port1 = a;
        port2 = b;
        this.voltage = value;
    }

    void updateBranch(Node s, Node e, float dt, float dv) {
        //pass
    }
}

class Capacitor extends Branch {
    Capacitor(int a, int b, float value) {
        super(a, b, value);
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
    }
}

class Inductor extends Branch {
    Inductor(int a, int b, float value) {
        super(a, b, value);
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
    private int numberOfNodes = 0;
    private int numberOfBranches = 0;
    float dt, dv;
    int[][] adjMatrix = new int[100][100];
    //Set<Node> nodeSet = new HashSet<Node>();
    Node[] nodeArray = new Node[100];
    //Set<Branch> branchSet = new HashSet<Branch>();
    Branch[] branchArray = new Branch[100];

    Circuit(float dt, float dv) {
        this.dt = dt;
        this.dv = dv;
    }

    void add(String type, int startNode, int endNode, float value) {
        adjMatrix[startNode][endNode] = 1;
        adjMatrix[endNode][startNode] = -1;
        //nodeSet.add(startNode);
        //nodeSet.add(endNode);
        numberOfNodes = Math.max(numberOfNodes, Math.max(startNode, endNode));

        switch (type) {
            case "R":
                branchArray[numberOfBranches++] = new Resistor(startNode, endNode, value);
                break;
            case "I":
                branchArray[numberOfBranches++] = new CurrentSource(startNode, endNode, value);
                break;
            case "V":
                branchArray[numberOfBranches++] = new VoltageSource(startNode, endNode, value);
                break;
            case "C":
                branchArray[numberOfBranches++] = new Capacitor(startNode, endNode, value);
                break;
            case "L":
                branchArray[numberOfBranches++] = new Inductor(startNode, endNode, value);
                break;
        }
    }

    void initCircuit() {
        for (int j = 0; j <= numberOfNodes; j++) nodeArray[j] = new Node(j);
    }

    void updateBranches() {
        for (int j = 0; branchArray[j] != null; j++)
            branchArray[j].updateBranch(nodeArray[branchArray[j].port1], nodeArray[branchArray[j].port2], dt, dv);
    }

    void updateNodes() {
        int cnt = 0;
        for (int i = 0; nodeArray[i] != null; i++) {
            nodeArray[i].newCurrent = 0;
            nodeArray[i].previousCurrent = 0;
            for (int j = 0; branchArray[j] != null; j++) {
                if (branchArray[j].port1 == cnt) {
                    nodeArray[i].newCurrent += branchArray[j].current;
                    nodeArray[i].previousCurrent += branchArray[j].previousCurrent;
                } else if (branchArray[j].port2 == cnt) {
                    nodeArray[i].newCurrent -= branchArray[j].current;
                    nodeArray[i].previousCurrent -= branchArray[j].previousCurrent;
                }
            }
            nodeArray[i].previousVoltage = nodeArray[i].voltage;
            nodeArray[i].current = (nodeArray[i].newCurrent + nodeArray[i].previousVoltage) / 2;
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
        for (int j = 0; branchArray[j] != null; j++)
            System.out.println("Branch: " + branchArray[j].getClass() + " voltage:" + branchArray[j].getVoltage(nodeArray[branchArray[j].port1], nodeArray[branchArray[j].port2]) + " current:" + branchArray[j].getCurrent());
        System.out.println("--------");
    }
}

public class Main {
    public static void main(String[] args) {
        Circuit circuit = new Circuit(0.1f, 0.1f);
        circuit.add("I", 0, 1, 10);
        circuit.add("R", 1, 2, 5);
        circuit.add("R", 2, 3, 6);
        circuit.add("C", 2, 0, 1);
        circuit.add("R", 3, 0, 4);
        circuit.initCircuit();
        for (int k = 0; k < 100; k++) {
            circuit.updateBranches();
            circuit.updateNodes();
            if (k % 10 == 0) circuit.printData();
        }
    }
}