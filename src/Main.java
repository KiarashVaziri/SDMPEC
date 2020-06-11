import java.util.HashSet;
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

    float getCurrent() {
        return current;
    }

    float getVoltage() {
        return voltage;
    }

    float getPower() {
        return voltage * current;
    }

    void updateBranch(Node port1, Node port2, float dt, float dv) {
    }
}

class Resistor extends Branch {
    Resistor(int a, int b, float r) {
        port1 = new Node(a);
        port2 = new Node(b);
        this.resistance = r;
        this.voltage = (float) (port1.getVoltage() - port2.getVoltage());
        this.current = voltage / r;
    }

    @Override
    void updateBranch(Node port1, Node port2, float dt, float dv) {
        current = (port1.voltage - port2.voltage) / resistance;
        previousCurrent = (port1.voltage - port2.voltage + dv) / resistance;
    }
}

class CurrentSource extends Branch {
    final float value;

    CurrentSource(int a, int b, float value) {
        port1 = new Node(a);
        port2 = new Node(b);
        this.voltage = (float) (port1.getVoltage() - port2.getVoltage());
        this.current = value;
        this.value = value;
    }

    @Override
    void updateBranch(Node port1, Node port2, float dt, float dv) {
        previousCurrent = current;
        current = value;
    }
}

class VoltageSource extends Branch {
    VoltageSource(int a, int b, int value) {
        port1 = new Node(a);
        port2 = new Node(b);
        this.voltage = value;
    }

    void updateBranch(Node port1, Node port2, float dt, float dv) {
        //pass
    }
}

class Capacitor extends Branch {
    Capacitor(int a, int b, int value) {
        port1 = new Node(a);
        port2 = new Node(b);
        this.capacity = value;
        this.voltage = (float) (port1.getVoltage() - port2.getVoltage());
    }

    void updateBranch(Node port1, Node port2, float dt, float dv) {
        current = (capacity * (port1.voltage - port1.previousCurrent - port2.voltage + port2.previousVoltage)) / dt;
        previousCurrent = (capacity * (port1.voltage - port1.previousCurrent - port2.voltage + port2.previousVoltage + dv)) / dt;
    }
}

class Inductor extends Branch {
    Inductor(int a, int b, int value) {
        port1 = new Node(a);
        port2 = new Node(b);
        this.inductance = value;
        this.voltage = (float) (port1.getVoltage() - port2.getVoltage());
    }

    void updateBranch(Node port1, Node port2, float dt, float dv) {
        current += ((port1.voltage - port1.previousVoltage - port2.voltage + port2.previousVoltage) * dt) / inductance;
        previousCurrent += ((port1.voltage - port1.previousVoltage - port2.voltage + port2.previousVoltage + dv) * dt) / inductance;
    }
}

class Circuit {
    private int numberOfNodes = 0;
    private int numberOfBranches = 0;
    int[][] adjMatrix = new int[100][100];
    Set<Node> nodeSet = new HashSet<Node>();
    //Node[] nodeArray=new Node[100];
    Set<Branch> branchSet = new HashSet<Branch>();

    //Branch[] branchArray=new Branch[100];
    void add(Branch branch, Node startNode, Node endNode) {
        adjMatrix[startNode.nodeNumber][endNode.nodeNumber] = 1;
        adjMatrix[endNode.nodeNumber][startNode.nodeNumber] = -1;
        nodeSet.add(startNode);
        nodeSet.add(endNode);
        //nodeArray[startNode.nodeNumber]=startNode;
        //nodeArray[endNode.nodeNumber]=endNode;
        branchSet.add(branch);
        //branchArray[numberOfBranches++]=branch;
    }

    void remove(Branch branch) {
        branchSet.remove(branch);
        adjMatrix[branch.port1.nodeNumber][branch.port2.nodeNumber] = 0;
        adjMatrix[branch.port2.nodeNumber][branch.port1.nodeNumber] = 0;
    }
}

public class Main {
    public static void main(String[] args) {
    }
}
