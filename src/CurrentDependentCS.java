public class CurrentDependentCS extends Branch {
    int type = 2;//F
    /*there are two types:
    1. dependent to a certain voltage
    2. dependent to a certain current */

    float gain;
    //type 2 demands an element
    Branch element;
    String dependent_element;

    CurrentDependentCS(String name, int i, int j, String elementName, float value) {
        super(name, i, j, value);
        this.name = name;
        port1 = i;
        port2 = j;
        this.gain = value;
        this.dependent_element = elementName;
    }

    void updateRelatedElement(Branch[] branchArray) {
        for (int i = 0; branchArray[i] != null; i++)
            if (branchArray[i].name.equals(dependent_element)) element = branchArray[i];
    }

    void updateBranch(Branch[] branches, float dt, float dv) {
        updateRelatedElement(branches);
        current = gain * element.current;
        previousCurrent = gain * element.previousCurrent;
    }
}
