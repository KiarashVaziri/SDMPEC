import com.sun.org.apache.xpath.internal.objects.XNodeSet;

class CurrentSource extends Branch {
    float offset;
    float amplitude;
    float frequency;
    float phase;

    CurrentSource(String name, int a, int b, float offset, float amplitude, float frequency, float phase) {
        super(name, a, b, offset);
        this.name = name;
        port1 = b;
        port2 = a;
        type_of_source = 1;//KCL
        //this.voltage = (float) (port1.getVoltage() - port2.getVoltage());
        this.offset = offset;
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.phase = phase;
        current = (float) (offset + amplitude * Math.sin(Math.toRadians(phase)));
    }

    @Override
    float getVoltage(Node a, Node b) {
        return b.voltage - a.voltage;
    }

    @Override
    void updateBranch(Node a, Node b, float dt, float dv, float time) {
        voltage = a.voltage - b.voltage;
        current = (float) (offset + amplitude * Math.sin(2 * Math.PI * frequency * time + Math.toRadians(phase)));
        previousCurrent = current;
        nextCurrent_negative = current;
        nextCurrent_plus = current;
        power = current * voltage;
    }

    @Override
    void updateBranchFinal(Node startNode, Node endNode, float dt, float dv, float time, int step) {
        voltage_t.add(voltage);
        current_t.add(current);
        power_t.add(power);
    }
}