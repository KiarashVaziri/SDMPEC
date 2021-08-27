package circuit.branch;

import circuit.branch.Branch;

public class Diode extends Branch {
    public Diode(String name, int startNode, int endNode, float value) {
        super(name, startNode, endNode, value);
        this.name = name;
        port1 = startNode;
        port2 = endNode;
        this.resistance = value;
    }
}
