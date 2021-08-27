package circuit.branch;

import circuit.Node;
import circuit.branch.Branch;

public class VoltageDependentVS extends Branch {

    /*there are two types:
    3. dependent to a certain voltage
    4. dependent to a certain current */

    float gain;
    int related_port1;
    int related_port2;

    public VoltageDependentVS(String name, int i, int j, int k, int m, float value) {
        super(name, i, j, value);
        this.name = name;
        independent = false;
        type = 3;//E
        type_of_source = 2;
        port1 = i;
        port2 = j;
        this.gain = value;
        related_port1 = k;
        related_port2 = m;
    }

    public float getVoltage(Node a, Node b) {
        return b.voltage - a.voltage;
    }

    public void updateBranch(Node[] nodes, float dt, float dv) {
        voltage = gain * (nodes[related_port1].voltage - nodes[related_port2].voltage);
        updateCurrent(nodes[port1],nodes[port2]);
        power = current * voltage;
    }

    @Override
    public void updateBranchFinal(Node startNode, Node endNode, float dt, float dv, float time, int step) {
        voltage_t.add(voltage);
        current_t.add(current);
        power_t.add(power);
    }

    void updateCurrent(Node s, Node e) {
        if (s.numberOfVS == 1)
            current = +s.expected_current;
        else if (e.numberOfVS == 1)
            current = -e.expected_current;
    }
}
