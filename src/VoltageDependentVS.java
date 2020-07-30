class VoltageDependentVS extends Branch {

    /*there are two types:
    3. dependent to a certain voltage
    4. dependent to a certain current */

    float gain;
    int related_port1;
    int related_port2;

    VoltageDependentVS(String name, int i, int j, int k, int m, float value) {
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

    float getVoltage(Node a, Node b) {
        return b.voltage - a.voltage;
    }

    void updateBranch(Node[] nodes, float dt, float dv) {
        voltage = gain * (nodes[related_port1].voltage - nodes[related_port2].voltage);
        power = current * voltage;
    }

    @Override
    void updateBranchFinal(Node startNode, Node endNode, float dt, float dv, float time, int step) {
        voltage_t.add(voltage);
        current_t.add(0f);
        power_t.add(power);
    }
}
