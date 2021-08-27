package circuit.branch;

import circuit.Node;
import circuit.branch.Branch;

public class Inductor extends Branch {

    public Inductor(String name, int a, int b, float value) {
        super(name, a, b, value);
        this.name = name;
        port1 = a;
        port2 = b;
        this.inductance = value;
    }

    public float getVoltage(Node s, Node e) {
        return s.voltage - e.voltage;
    }

    public void updateBranch(Node s, Node e, float dt, float dv, float time) {
        voltage = s.voltage - e.voltage;
        //current += (s.voltage - e.voltage) * dt / inductance;
        current = previousCurrent_t + (s.voltage - e.voltage) * dt / inductance;
        //previousCurrent += ((s.voltage - s.previousVoltage - e.voltage + e.previousVoltage + dv) * dt) / inductance;
        //nextCurrent_plus += (s.voltage - e.voltage + dv) * dt / inductance;
        nextCurrent_plus = current + dv * dt / inductance;
        //nextCurrent_negative += (s.voltage - e.voltage - dv) * dt / inductance;
        nextCurrent_negative = current - dv * dt / inductance;
        power = current * voltage;
    }

    public void updateBranchFinal(Node startNode, Node endNode, float dt, float dv, float time, int step) {
        voltage_t.add(voltage);
        current_t.add(current);
        previousCurrent_t = current;
        power_t.add(power);
    }
}