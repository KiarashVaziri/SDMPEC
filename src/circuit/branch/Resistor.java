package circuit.branch;

import circuit.Node;
import circuit.branch.Branch;

public class Resistor extends Branch {

    public Resistor(String name, int a, int b, float r) {
        super(name, a, b, r);
        this.name = name;
        port1 = a;
        port2 = b;
        this.resistance = r;
    }

    @Override
    public float getVoltage(Node a, Node b) {
        return a.voltage - b.voltage;
    }

    @Override
    public void updateBranch(Node startNode, Node endNode, float dt, float dv, float time) {
        voltage = startNode.voltage - endNode.voltage;
        current = voltage / resistance;
        //previousCurrent = (startNode.voltage - endNode.voltage + dv) / resistance;
        nextCurrent_plus = (startNode.voltage - endNode.voltage + dv) / resistance;
        nextCurrent_negative = (startNode.voltage - endNode.voltage - dv) / resistance;
        power = current * voltage;
    }

    @Override
    public void updateBranchFinal(Node startNode, Node endNode, float dt, float dv, float time, int step) {
        //voltage_t.add(startNode.voltage_t.get(step) - endNode.voltage_t.get(step));
        voltage_t.add(voltage);
        //current_t.add(voltage_t.get(step) / resistance);
        current_t.add(current);
        power_t.add(power);
    }
}