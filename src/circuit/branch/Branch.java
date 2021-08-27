import java.util.ArrayList;

class Branch {
    String name;
    int port1, port2;
    boolean independent = true;
    //independence type
    int type = 0;
    float resistance;
    float capacity;
    float inductance;

    float current = 0;
    float previousCurrent = 0;
    float previousCurrent_t = 0;
    float nextCurrent_plus = 0;
    float nextCurrent_negative = 0;
    ArrayList<Float> current_t = new ArrayList<Float>();

    float voltage;
    float previousVoltage;
    float previousVoltage_t;
    ArrayList<Float> voltage_t = new ArrayList<Float>();

    float power;
    ArrayList<Float> power_t = new ArrayList<Float>();

    //for KCL & KVL
    int type_of_source = 0;
    /* two types of sources:
       1. Current source
       2. Voltage source */

    public Branch(String name, int startNode, int endNode, float value) {
    }

    float getCurrent() {
        return current;
    }

    float getVoltage(Node a, Node b) {
        return a.voltage - b.voltage;
    }

    //for independent ones
    void updateBranch(Node startNode, Node endNode, float dt, float dv, float time) {
    }

    //dependent type 1 & 3
    void updateBranch(Node[] nodes, float dt, float dv) {
    }

    //dependent type 2
    void updateBranch(Branch[] branches, float dt, float dv) {
    }

    //dependent type 4
    void updateBranch(Branch[] branches, Node[] nodes, float dt, float dv) {
    }

    //for each step
    void updateBranchFinal(Node startNode, Node endNode, float dt, float dv, float time, int step) {
    }
}
