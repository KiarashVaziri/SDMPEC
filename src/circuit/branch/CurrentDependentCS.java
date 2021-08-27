package circuit.branch;

import circuit.Node;
import circuit.branch.Branch;

public class CurrentDependentCS extends Branch {

    /*there are two types:
    1. dependent to a certain voltage
    2. dependent to a certain current */

    float gain;
    //type 2 demands an element
    Branch element;
    String dependent_element;

    public CurrentDependentCS(String name, int i, int j, String elementName, float value) {
        super(name, i, j, value);
        this.name = name;
        independent = false;
        type = 2;//F
        type_of_source = 1;//KCL
        port1 = j;
        port2 = i;
        this.gain = value;
        this.dependent_element = elementName;
    }

    void updateRelatedElement(Branch[] branchArray) {
        for (int i = 0; branchArray[i] != null; i++)
            if (branchArray[i].name.equals(dependent_element)) element = branchArray[i];
    }

    public float getVoltage(Node a, Node b) {
        return b.voltage - a.voltage;
    }

    public void updateBranch(Branch[] branches, float dt, float dv) {
        updateRelatedElement(branches);
        current = gain * element.current;
        nextCurrent_negative = current;
        nextCurrent_plus = current;
        previousCurrent = gain * element.previousCurrent;
        power = current * voltage;
    }

    @Override
    public void updateBranchFinal(Node startNode, Node endNode, float dt, float dv, float time, int step) {
        voltage = startNode.voltage - endNode.voltage;
        current_t.add(current);
        voltage_t.add(voltage);
        power_t.add(power);
    }
}
