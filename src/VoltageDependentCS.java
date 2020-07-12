class VoltageDependentCS extends Branch {

    /*there are two types:
    1. dependent to a certain voltage
    2. dependent to a certain current */

    float gain;
    int related_port1;
    int related_port2;

    VoltageDependentCS(String name, int i, int j, int k, int m, float value) {
        super(name, i, j, value);
        this.name = name;
        independence = false;
        type = 1;//G
        port1 = j;
        port2 = i;
        this.gain = value;
        related_port1 = k;
        related_port2 = m;

        //this.current=value*(nodeArray[related_port1]-nodeArray[related_port2]);
    }

    float getVoltage(Node a, Node b) {
        return b.voltage - a.voltage;
    }

    void updateBranch(Node[] nodes, float dt, float dv) {
        //previousCurrent = gain * (nodes[related_port1].previousVoltage - nodes[related_port2].previousVoltage);
        previousCurrent = current;

        current = gain * (nodes[related_port1].voltage - nodes[related_port2].voltage);
        //System.out.println("vdcs current: "+current+" ,nodes[related_port1].voltage: "+nodes[related_port1].voltage);
        //should "dv" be added to the voltage difference?
        //previousCurrent = gain * (nodes[related_port1].voltage - nodes[related_port2].voltage + dv);

    }
}
