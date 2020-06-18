class DependentCurrentSource extends Branch {
    int type = 0;
    /*there are two types:
     1. dependent to a certain voltage
     2. dependent to a certain current */

    int related_port1;
    int related_port2;

    //type 2 demands an element
    Branch element;
    String elementName;

    DependentCurrentSource(int i, int j, float value, int k, int m) {
        super(i, j, value);
        port1 = i;
        port2 = j;
        type = 1;
        related_port1 = k;
        related_port2 = m;
        //this.current=value*(nodeArray[related_port1]-nodeArray[related_port2]);
    }

    DependentCurrentSource(int i, int j, float value, String elementName) {
        super(i, j, value);
        port1 = i;
        port2 = j;
        type = 2;
        this.elementName = elementName;
    }

    void updateRelatedElement(Branch[] branchArray) {
        for (int i = 0; branchArray[i] != null; i++)
            if (branchArray[i].name.equals(elementName)) element = branchArray[i];
    }

    void updateBranch(Node s, Node e, float dt, float dv) {
    }

}