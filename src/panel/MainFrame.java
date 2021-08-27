package panel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        setSize(815, 700);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container content = this.getContentPane();
        content.setBackground(Color.DARK_GRAY);
        JRootPane root;
        root = this.getRootPane();
        LayoutManager mgr;
        mgr = new GroupLayout(content);
        this.setLayout(mgr);

        DataPanel d = new DataPanel();
        add(d);

        // CircuitPanel c = new CircuitPanel();
        // add(c);

        ResultPanel r = new ResultPanel(d);
        add(r);

        SharifPanel s = new SharifPanel();
        add(s);
        this.setLocation(450, 100);
        this.setVisible(true);
    }
}
