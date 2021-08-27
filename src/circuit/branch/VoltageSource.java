package circuit.branch;

import circuit.Node;
import circuit.branch.Branch;

public class VoltageSource extends Branch {

    float offset;
    float amplitude;
    float frequency;
    float phase;

    public VoltageSource(String name, int a, int b, float offset, float amplitude, float frequency, float phase) {
        super(name, a, b, offset);
        this.name = name;
        port1 = a;
        port2 = b;
        type_of_source = 2;
        //this.voltage = (float) (port1.getVoltage() - port2.getVoltage());
        this.offset = offset;
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.phase = phase;
        voltage = (float) (offset + amplitude * Math.sin(Math.toRadians(phase)));
    }

    public void updateBranch(Node s, Node e, float dt, float dv, float time) {
        voltage = (float) (offset + amplitude * Math.sin(2 * Math.PI * frequency * time + Math.toRadians(phase)));
        updateCurrent(s, e);
        power = current * voltage;
    }

    @Override
    public void updateBranchFinal(Node startNode, Node endNode, float dt, float dv, float time, int step) {
        voltage_t.add(voltage);
        current_t.add(current);
        previousVoltage_t = voltage;
        power_t.add(power);
    }

    void updateCurrent(Node s, Node e) {
        if (s.numberOfVS == 1)
            current = -s.expected_current;
        else if (e.numberOfVS == 1)
            current = +e.expected_current;
    }
}