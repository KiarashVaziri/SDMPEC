class VoltageSource extends Branch {
    float offset;
    float amplitude;
    float frequency;
    float phase;

    VoltageSource(String name, int a, int b, float offset, float amplitude, float frequency, float phase) {
        super(name, a, b, offset);
        this.name = name;
        port1 = a;
        port2 = b;
        type_of_source = 2;
        //this.voltage = (float) (port1.getVoltage() - port2.getVoltage());
        this.offset = offset;
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.phase = phase;
        voltage = (float) (offset + amplitude * Math.sin(Math.toRadians(phase)));
    }

    void updateBranch(Node s, Node e, float dt, float dv, float time) {
        voltage = (float) (offset + amplitude * Math.sin(2 * Math.PI * frequency * time + Math.toRadians(phase)));
        power = current * voltage;
    }

    @Override
    void updateBranchFinal(Node startNode, Node endNode, float dt, float dv, float time, int step) {
        voltage_t.add(voltage);
        current_t.add(0f);
        previousVoltage_t = voltage;
        power_t.add(power);
    }
}