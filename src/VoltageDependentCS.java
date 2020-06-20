class VoltageDependentCS extends Branch {
    int type = 1;//G
    /*there are two types:
    1. dependent to a certain voltage
    2. dependent to a certain current */

    float gain;
    int related_port1;
    int related_port2;

    VoltageDependentCS(String name, int i, int j, int k, int m, float value) {
        super(name, i, j, value);
        this.name = name;
        port1 = i;
        port2 = j;
        this.gain = value;
        related_port1 = k;
        related_port2 = m;
        //this.current=value*(nodeArray[related_port1]-nodeArray[related_port2]);
    }

    void updateBranch(Node[] nodes, float dt, float dv) {
        current = gain * (nodes[related_port1].voltage - nodes[related_port2].voltage);
        //should "dv" be added to the voltage difference?
        //previousCurrent = gain * (nodes[related_port1].voltage - nodes[related_port2].voltage + dv);
        previousCurrent = gain * (nodes[related_port1].voltage - nodes[related_port2].voltage);
    }
}
