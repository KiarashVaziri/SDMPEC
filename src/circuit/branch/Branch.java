package circuit.branch;

import circuit.Node;

import java.util.ArrayList;

public class Branch {
    public String name;
    public int port1;
    public int port2;
    public boolean independent = true;
    //independence type
    public int type = 0;
    float resistance;
    float capacity;
    float inductance;

    public float current = 0;
    public float previousCurrent = 0;
    float previousCurrent_t = 0;
    public float nextCurrent_plus = 0;
    public float nextCurrent_negative = 0;
    public ArrayList<Float> current_t = new ArrayList<Float>();

    public float voltage;
    float previousVoltage;
    float previousVoltage_t;
    public ArrayList<Float> voltage_t = new ArrayList<Float>();

    float power;
    public ArrayList<Float> power_t = new ArrayList<Float>();

    //for KCL & KVL
    public int type_of_source = 0;
    /* two types of sources:
       1. Current source
       2. Voltage source */

    public Branch(String name, int startNode, int endNode, float value) {
    }

    public float getCurrent() {
        return current;
    }

    public float getVoltage(Node a, Node b) {
        return a.voltage - b.voltage;
    }

    //for independent ones
    public void updateBranch(Node startNode, Node endNode, float dt, float dv, float time) {
    }

    //dependent type 1 & 3
    public void updateBranch(Node[] nodes, float dt, float dv) {
    }

    //dependent type 2
    public void updateBranch(Branch[] branches, float dt, float dv) {
    }

    //dependent type 4
    public void updateBranch(Branch[] branches, Node[] nodes, float dt, float dv) {
    }

    //for each step
    public void updateBranchFinal(Node startNode, Node endNode, float dt, float dv, float time, int step) {
    }
}
