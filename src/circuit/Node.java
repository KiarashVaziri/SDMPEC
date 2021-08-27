package circuit;

import circuit.branch.Branch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Node {
    int nodeNumber;
    float previousVoltage = 0;
    float previousVoltage_t = 0;
    public float voltage = 0;
    ArrayList<Float> voltage_t = new ArrayList<Float>();
    float previousCurrent = 0;
    float current = 0;
    float newCurrent = 0;

    //for unions
    boolean needs_parent = false;
    int parentNode;
    int unionNumber;
    boolean _added = false;
    Set<Node> neighbors = new HashSet<Node>();
    Branch parentElement;
    int parent_element;

    //for voltage source current
    public int numberOfVS;
    public float expected_current = 0;

    //-2 error
    boolean toBeChecked = true;
    Set<Branch> neighborCurrentSources = new HashSet<Branch>();

    //-5 error
    boolean connected = false;
    boolean[] neighbor_nodes = new boolean[100];

    public Node(int a) {
        nodeNumber = a;
        unionNumber = nodeNumber;
        parentNode = nodeNumber;
        voltage_t.add(0f);
    }

    double getVoltage() {
        return voltage;
    }
}
