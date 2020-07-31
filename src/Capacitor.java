class Capacitor extends Branch {
    Capacitor(String name, int a, int b, float value) {
        super(name, a, b, value);
        this.name = name;
        port1 = a;
        port2 = b;
        this.capacity = value;
    }

    float getVoltage(Node s, Node e) {
        return s.voltage - e.voltage;
    }

    void updateBranch(Node s, Node e, float dt, float dv, float time) {
        //previousVoltage = voltage;
        voltage = s.voltage - e.voltage;

        //current = capacity * (voltage - previousVoltage) / dt;
        current = capacity * (voltage - previousVoltage_t) / dt;

        nextCurrent_plus = current + capacity * dv / dt;
        nextCurrent_negative = current - capacity * dv / dt;
        //previousCurrent = capacity * (s.voltage - s.previousVoltage - e.voltage + e.previousVoltage + dv) / dt;

        power = current * voltage;
    }

    @Override
    void updateBranchFinal(Node startNode, Node endNode, float dt, float dv, float time, int step) {
        voltage_t.add(voltage);
        current_t.add(current);
        previousVoltage_t = voltage;
        power_t.add(power);
    }
}