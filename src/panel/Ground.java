package panel;

import circuit.Node;

class Ground extends Node {
    static final double voltage = 0;
    Node[] node_array;

    Ground(int a) {
        super(a);
    }
}
