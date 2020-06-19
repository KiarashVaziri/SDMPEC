public class CurrentDependentCS extends Branch {
    int type = 2;
    /*there are two types:
    1. dependent to a certain voltage
    2. dependent to a certain current */

    float gain;
    //type 2 demands an element
    Branch element;
    String elementName;

    CurrentDependentCS(int i, int j, float value, String elementName) {
        super(i, j, value);
        port1 = i;
        port2 = j;
        type = 2;
        this.gain = value;
        this.elementName = elementName;
    }

    void updateRelatedElement(Branch[] branchArray) {
        for (int i = 0; branchArray[i] != null; i++)
            if (branchArray[i].name.equals(elementName)) element = branchArray[i];
    }

    void updateBranch(Branch[] branches, float dt, float dv) {
        updateRelatedElement(branches);
        current = gain*element.current;
        previousCurrent = gain*element.previousCurrent;
    }
}
