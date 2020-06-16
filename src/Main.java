import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
    Node port1, port2;
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

    float getVoltage() {
        return port1.voltage - port2.voltage;
    }

    float getPower() {
        return getVoltage() * current;
    }

    void updateBranch(float dt, float dv) {
    }
}

class Resistor extends Branch {
    Resistor(int a, int b, float r) {
        super(a, b, r);
        port1 = new Node(a);
        port2 = new Node(b);
        this.resistance = r;
        this.voltage = (float) (port1.getVoltage() - port2.getVoltage());
        this.current = voltage / r;
    }

    @Override
    void updateBranch(float dt, float dv) {
        current = (port1.voltage - port2.voltage) / resistance;
        previousCurrent = (port1.voltage - port2.voltage + dv) / resistance;
    }
}

class CurrentSource extends Branch {
    final float value;

    CurrentSource(int a, int b, float value) {
        super(a, b, value);
        port1 = new Node(a);
        port2 = new Node(b);
        this.voltage = (float) (port1.getVoltage() - port2.getVoltage());
        this.current = value;
        this.value = value;
    }

    @Override
    void updateBranch(float dt, float dv) {
        previousCurrent = current;
        current = value;
    }
}

class VoltageSource extends Branch {
    VoltageSource(int a, int b, float value) {
        super(a, b, value);
        port1 = new Node(a);
        port2 = new Node(b);
        this.voltage = value;
    }

    void updateBranch(float dt, float dv) {
        //pass
    }
}

class Capacitor extends Branch {
    Capacitor(int a, int b, float value) {
        super(a, b, value);
        port1 = new Node(a);
        port2 = new Node(b);
        this.capacity = value;
        this.voltage = (float) (port1.getVoltage() - port2.getVoltage());
    }

    void updateBranch(float dt, float dv) {
        current = (capacity * (port1.voltage - port1.previousCurrent - port2.voltage + port2.previousVoltage)) / dt;
        previousCurrent = (capacity * (port1.voltage - port1.previousCurrent - port2.voltage + port2.previousVoltage + dv)) / dt;
    }
}

class Inductor extends Branch {
    Inductor(int a, int b, float value) {
        super(a, b, value);
        port1 = new Node(a);
        port2 = new Node(b);
        this.inductance = value;
        this.voltage = (float) (port1.getVoltage() - port2.getVoltage());
    }

    void updateBranch(float dt, float dv) {
        current += ((port1.voltage - port1.previousVoltage - port2.voltage + port2.previousVoltage) * dt) / inductance;
        previousCurrent += ((port1.voltage - port1.previousVoltage - port2.voltage + port2.previousVoltage + dv) * dt) / inductance;
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
    Branch[] branchArray=new Branch[100];
    Circuit(float dt, float dv) {
        this.dt = dt;
        this.dv = dv;
    }

    void add(String type, int startNode, int endNode, float value) {
        adjMatrix[startNode][endNode] = 1;
        adjMatrix[endNode][startNode] = -1;
        //nodeSet.add(startNode);
        //nodeSet.add(endNode);

        if (type.equals("R")) {
            branchArray[numberOfBranches] = new Resistor(startNode, endNode, value);
            numberOfBranches++;
        } else if (type.equals("I")) {
            branchArray[numberOfBranches] = new CurrentSource(startNode, endNode, value);
            numberOfBranches++;
        } else if (type.equals("V")) {
            branchArray[numberOfBranches] = new VoltageSource(startNode, endNode, value);
            numberOfBranches++;
        } else if (type.equals("C")) {
            branchArray[numberOfBranches] = new Capacitor(startNode, endNode, value);
            numberOfBranches++;
        } else if (type.equals("L")) {
            branchArray[numberOfBranches] = new Inductor(startNode, endNode, value);
            numberOfBranches++;
        }

        //branchSet.add(element);

    }

    void initCircuit() {
        int k = 0;
        for(int j=0;j<branchArray.length && branchArray[j]!=null;j++){
            k = Math.max(Math.max(branchArray[j].port1.nodeNumber, branchArray[j].port2.nodeNumber), k);
        }
        for (int i = 0; i <= k; i++)
            nodeArray[i] = new Node(i);
    }

    //incomplete
    void remove(Branch branch) {
        adjMatrix[branch.port1.nodeNumber][branch.port2.nodeNumber] = 0;
        adjMatrix[branch.port2.nodeNumber][branch.port1.nodeNumber] = 0;
    }

    void updateBranches() {
        for(int j=0;j<branchArray.length && branchArray[j]!=null;j++)
            branchArray[j].updateBranch(dt, dv);
    }

    void updateNodes() {
        int cnt = 0;
        for (int i = 0; i < nodeArray.length && nodeArray[i]!=null; i++) {
            nodeArray[i].newCurrent = 0;
            nodeArray[i].previousCurrent = 0;
            for(int j=0;j<branchArray.length && branchArray[j]!=null;j++){
                if (branchArray[j].port1.nodeNumber == cnt) {
                    nodeArray[i].newCurrent += branchArray[j].current;
                    nodeArray[i].previousCurrent += branchArray[j].previousCurrent;
                } else if (branchArray[j].port2.nodeNumber == cnt) {
                    nodeArray[i].newCurrent += branchArray[j].current;
                    nodeArray[i].previousCurrent += branchArray[j].previousCurrent;
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
        for (int i = 0; i < nodeArray.length && nodeArray[i]!=null; i++)
            System.out.println("Node: " + nodeArray[i].nodeNumber + " voltage:" + nodeArray[i].voltage + " current:" + nodeArray[i].current);
        System.out.println();
        for(int j=0;j<branchArray.length && branchArray[j]!=null;j++)
            System.out.println("Branch: " + branchArray[j].getClass() + " voltage:" + branchArray[j].getVoltage() + " current:" + branchArray[j].getCurrent());
        System.out.println("--------");
    }
}

public class Main {
    public static void main(String[] args) {
        Circuit circuit = new Circuit(0.1f, 0.1f);
        CurrentSource currentSource = new CurrentSource(0, 1, 10);
        circuit.add("I", 0, 1, 10);
        circuit.add("R", 1, 2, 5);
        circuit.add("R", 2, 3, 6);
        circuit.add("R", 2, 0, 10);
        circuit.add("R", 3, 0, 4);
        circuit.initCircuit();
        for (int k = 0; k < 1000; k++) {
            circuit.updateBranches();
            circuit.updateNodes();
            if (k % 200 == 0) circuit.printData();
        }
    }
}
