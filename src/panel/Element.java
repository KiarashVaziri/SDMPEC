package panel;

import circuit.branch.Branch;

import javax.swing.*;

class Element {
    int dx, dy, n1i, n1j, n2i, n2j;

    Element(JPanel frame, int locationMatrix[][], char type, Branch branch, int node1, int node2) {
        dx = (node2 - 1) % 8 - (node1 - 1) % 8;
        dy = (node1 - 1) / 8 - (node2 - 1) / 8;
        n1i = ((node1 - 1) % 8) * 2;
        n1j = (5 - (node1 - 1) / 8) * 2;
        n2i = ((node2 - 1) % 8) * 2;
        n2j = (5 - (node2 - 1) / 8) * 2;
        Draw(frame, locationMatrix, type, branch, node1, node2, dx, dy);
    }

    void Draw(JPanel frame, int locationMatrix[][], char type, Branch branch, int node1, int node2, int dx, int dy) {
        n1i = ((node1 - 1) % 8) * 2;
        n1j = (5 - (node1 - 1) / 8) * 2;
        n2i = ((node2 - 1) % 8) * 2;
        n2j = (5 - (node2 - 1) / 8) * 2;
        if (node1 == 0) {
            if (n2i + 1 <= 11 && locationMatrix[n2i + 2][n2j] == 0)
                node1 = node2 + 1;
            else if (n2i - 1 >= 2 && locationMatrix[n2i - 2][n2j] == 0)
                node1 = node2 - 1;
            else if (n2j + 1 <= 9 && locationMatrix[n2i][n2j + 2] == 0)
                node1 = node2 - 8;
            else if (n2j - 1 >= 0 && locationMatrix[n2i][n2j - 2] == 0)
                node1 = node2 + 8;
            n1i = ((node1 - 1) % 8) * 2;
            n1j = (5 - (node1 - 1) / 8) * 2;
            dx = (node2 - 1) % 8 - (node1 - 1) % 8;
            dy = (node1 - 1) / 8 - (node2 - 1) / 8;
            GNDPanel g = new GNDPanel(node1);
            frame.add(g);
            Draw(frame, locationMatrix, type, branch, node1, node2, dx, dy);
        } else if (node2 == 0) {
            if (n1i + 1 <= 9 && locationMatrix[n1i + 2][n1j] == 0)
                node2 = node1 + 1;
            else if (n1i - 1 >= 0 && locationMatrix[n1i - 2][n1j] == 0)
                node2 = node1 - 1;
            else if (n1j + 1 <= 9 && locationMatrix[n1i][n1j + 2] == 0)
                node2 = node1 - 8;
            else if (n1j - 1 >= 0 && locationMatrix[n1i][n1j - 2] == 0)
                node2 = node1 + 8;
            n2i = ((node2 - 1) % 8) * 2;
            n2j = (5 - (node2 - 1) / 8) * 2;
            dx = (node2 - 1) % 8 - (node1 - 1) % 8;
            dy = (node1 - 1) / 8 - (node2 - 1) / 8;
            GNDPanel g = new GNDPanel(node2);
            frame.add(g);
            Draw(frame, locationMatrix, type, branch, node1, node2, dx, dy);
        } else if ((Math.abs(n1i - n2i) == 2 && n1j == n2j && locationMatrix[(n1i + n2i) / 2][n1j] == 0) || (Math.abs(n1j - n2j) == 2 && n1i == n2i && locationMatrix[n1i][(n1j + n2j) / 2] == 0)) {
            locationMatrix[n1i][n1j] = 1;
            locationMatrix[n2i][n2j] = 1;
            locationMatrix[(n1i + n2i) / 2][(n1j + n2j) / 2] = 1;
            if (type == 'R') {
                ResistorPanel r = new ResistorPanel(node1, node2, branch.name);
                frame.add(r);
            } else if (type == 'C') {
                CapacitorPanel c = new CapacitorPanel(node1, node2, branch.name);
                frame.add(c);
            } else if (type == 'L') {
                InductorPanel l = new InductorPanel(node1, node2, branch.name);
                frame.add(l);
            } else if (type == 'V') {
                VoltageDCPanel v = new VoltageDCPanel(node1, node2, branch.name);
                frame.add(v);
            } else if (type == 'I') {
                CurrentDCPanel i = new CurrentDCPanel(node1, node2, branch.name);
                frame.add(i);
            } else if (type == 'H') {
                DependentVoltagePanel h = new DependentVoltagePanel(node1, node2, branch.name);
                frame.add(h);
            } else if (type == 'G') {
                DependentCurrentPanel e = new DependentCurrentPanel(node1, node2, branch.name);
                frame.add(e);
            } else if (type == 'E') {
                DependentVoltagePanel g = new DependentVoltagePanel(node1, node2, branch.name);
                frame.add(g);
            } else if (type == 'F') {
                DependentCurrentPanel f = new DependentCurrentPanel(node1, node2, branch.name);
                frame.add(f);
            } else if (type == 'D') {
                DiodePanel d = new DiodePanel(node1, node2, branch.name);
                frame.add(d);
            }
        } else {
            if (dx == 0) {
                if (dy != 0 && locationMatrix[n1i][n1j + (dy / Math.abs(dy))] == 0 && locationMatrix[n1i][n1j + 2 * (dy / Math.abs(dy))] == 0) {
                    WirePanel w = new WirePanel(node1, node1 - 8 * dy / Math.abs(dy));
                    frame.add(w);
                    locationMatrix[n1i][n1j + (dy / Math.abs(dy))] = 1;
                    locationMatrix[n1i][n1j + 2 * (dy / Math.abs(dy))] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 - 8 * dy / Math.abs(dy), node2, dx, (dy / Math.abs(dy)) * (Math.abs(dy) - 1));
                } else if (n1i + 1 <= 13 && locationMatrix[n1i + 1][n1j] == 0 && locationMatrix[n1i + 2][n1j] == 0) {
                    WirePanel w = new WirePanel(node1, node1 + 1);
                    frame.add(w);
                    locationMatrix[n1i + 1][n1j] = 1;
                    locationMatrix[n1i + 2][n1j] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 + 1, node2, dx - 1, dy);
                } else if (n1i - 1 >= 0 && locationMatrix[n1i - 1][n1j] == 0 && locationMatrix[n1i - 2][n1j] == 0) {
                    WirePanel w = new WirePanel(node1, node1 - 1);
                    frame.add(w);
                    locationMatrix[n1i - 1][n1j] = 1;
                    locationMatrix[n1i - 2][n1j] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 - 1, node2, dx + 1, dy);
                }
            } else if (dy == 0) {
                if (dx != 0 && locationMatrix[n1i + (dx / Math.abs(dx))][n1j] == 0 && locationMatrix[n1i + 2 * (dx / Math.abs(dx))][n1j] == 0) {
                    WirePanel w = new WirePanel(node1, node1 + dx / Math.abs(dx));
                    frame.add(w);
                    locationMatrix[n1i + (dx / Math.abs(dx))][n1j] = 1;
                    locationMatrix[n1i + 2 * (dx / Math.abs(dx))][n1j] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 + dx / Math.abs(dx), node2, (dx / Math.abs(dx)) * (Math.abs(dx) - 1), dy);
                } else if (n1j + 1 <= 9 && locationMatrix[n1i][n1j + 1] == 0 && locationMatrix[n1i][n1j + 2] == 0) {
                    WirePanel w = new WirePanel(node1, node1 - 8);
                    frame.add(w);
                    locationMatrix[n1i][n1j + 1] = 1;
                    locationMatrix[n1i][n1j + 2] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 - 8, node2, dx, dy - 1);
                } else if (n1j - 1 >= 0 && locationMatrix[n1i][n1j - 1] == 0 && locationMatrix[n1i][n1j - 2] == 0) {
                    WirePanel w = new WirePanel(node1, node1 + 8);
                    frame.add(w);
                    locationMatrix[n1i][n1j - 1] = 1;
                    locationMatrix[n1i][n1j - 2] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 + 8, node2, dx, dy + 1);
                }
            } else {
                if (dy != 0 && locationMatrix[n1i][n1j + (dy / Math.abs(dy))] == 0 && locationMatrix[n1i][n1j + 2 * (dy / Math.abs(dy))] == 0) {
                    WirePanel w = new WirePanel(node1, node1 - 8 * dy / Math.abs(dy));
                    frame.add(w);
                    locationMatrix[n1i][n1j + (dy / Math.abs(dy))] = 1;
                    locationMatrix[n1i][n1j + 2 * (dy / Math.abs(dy))] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 - 8 * dy / Math.abs(dy), node2, dx, (dy / Math.abs(dy)) * (Math.abs(dy) - 1));
                } else if (dx != 0 && locationMatrix[n1i + (dx / Math.abs(dx))][n1j] == 0 && locationMatrix[n1i + 2 * (dx / Math.abs(dx))][n1j] == 0) {
                    WirePanel w = new WirePanel(node1, node1 + dx / Math.abs(dx));
                    frame.add(w);
                    locationMatrix[n1i + (dx / Math.abs(dx))][n1j] = 1;
                    locationMatrix[n1i + 2 * (dx / Math.abs(dx))][n1j] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 + dx / Math.abs(dx), node2, (dx / Math.abs(dx)) * (Math.abs(dx) - 1), dy);
                } else if (n1i + 1 <= 13 && locationMatrix[n1i + 1][n1j] == 0 && locationMatrix[n1i + 2][n1j] == 0) {
                    WirePanel w = new WirePanel(node1, node1 + 1);
                    frame.add(w);
                    locationMatrix[n1i + 1][n1j] = 1;
                    locationMatrix[n1i + 2][n1j] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 + 1, node2, dx - 1, dy);
                } else if (n1i - 1 >= 0 && locationMatrix[n1i - 1][n1j] == 0 && locationMatrix[n1i - 2][n1j] == 0) {
                    WirePanel w = new WirePanel(node1, node1 - 1);
                    frame.add(w);
                    locationMatrix[n1i - 1][n1j] = 1;
                    locationMatrix[n1i - 2][n1j] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 - 1, node2, dx + 1, dy);
                } else if (n1j + 1 <= 9 && locationMatrix[n1i][n1j + 1] == 0 && locationMatrix[n1i][n1j + 2] == 0) {
                    WirePanel w = new WirePanel(node1, node1 - 8);
                    frame.add(w);
                    locationMatrix[n1i][n1j + 1] = 1;
                    locationMatrix[n1i][n1j + 2] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 - 8, node2, dx, dy - 1);
                } else if (n1j - 1 >= 0 && locationMatrix[n1i][n1j - 1] == 0 && locationMatrix[n1i][n1j - 2] == 0) {
                    WirePanel w = new WirePanel(node1, node1 + 8);
                    frame.add(w);
                    locationMatrix[n1i][n1j - 1] = 1;
                    locationMatrix[n1i][n1j - 2] = 1;
                    Draw(frame, locationMatrix, type, branch, node1 + 8, node2, dx, dy + 1);
                }
            }
        }
    }
}
