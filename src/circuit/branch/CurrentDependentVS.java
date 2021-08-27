class CurrentDependentVS extends Branch {

    /*there are two types:
    1. dependent to a certain voltage
    2. dependent to a certain current */

    float gain;
    //type 2 demands an element
    Branch element;
    String dependent_element;

    CurrentDependentVS(String name, int i, int j, String elementName, float value) {
        super(name, i, j, value);
        this.name = name;
        independent = false;
        type = 4;//H
        type_of_source = 2;
        port1 = i;
        port2 = j;
        this.gain = value;
        this.dependent_element = elementName;
    }

    void updateRelatedElement(Branch[] branchArray) {
        for (int i = 0; branchArray[i] != null; i++)
            if (branchArray[i].name.equals(dependent_element)) element = branchArray[i];
    }

    float getVoltage(Node a, Node b) {
        return b.voltage - a.voltage;
    }

    void updateBranch(Branch[] branches,Node[] nodes, float dt, float dv) {
        updateRelatedElement(branches);
        voltage = gain * element.current;
        updateCurrent(nodes[port1],nodes[port2]);
        power = current * voltage;
    }

    @Override
    void updateBranchFinal(Node startNode, Node endNode, float dt, float dv, float time, int step) {
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
