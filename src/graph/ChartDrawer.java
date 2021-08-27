package graph;

import java.awt.*;
import java.util.ArrayList;

public class ChartDrawer extends Canvas {
    int type, size;
    ArrayList<Float> list;
    float duration;
    float max = 0, min = 0, scale;
    String order;
    String orderOfTime;

    /* We have 3 types of outputs:
       1.voltage
       2.current
       3.power  */

    public ChartDrawer(int type, ArrayList<Float> data, float duration) {
        this.type = type;
        list = data;
        this.duration = duration;
        for (int i = 0; i < list.size(); i++) {
            if (max < list.get(i)) max = list.get(i);
            if (min > list.get(i)) min = list.get(i);
        }
        size = list.size();
        if (Math.abs(max) > Math.abs(min)) scale = max;
        else scale = Math.abs(min);

        if (scale >= 1000) order = "k";
        else if (scale < 1000 && scale > 0.1f) order = "";
        else if (scale < 0.1f) order = "m";

        if (duration >= 0.1f) orderOfTime = "";
        else if (duration < 0.1f && duration >= 1E-4) orderOfTime = "m";
        else if (duration < 1E-4) orderOfTime = "u";
    }

    public void paint(Graphics graphics) {
        setBackground(Color.black);
        int startX = 60, startY = 340, width, height;

        //Y-axis
        for (int i = 0; i <= 800; i += 80) {
            Float value = 0f;
            if (orderOfTime.equals("")) value = duration * (float) i / 800;
            else if (orderOfTime.equals("m")) value = 1000 * duration * (float) i / 800;
            else if (orderOfTime.equals("u")) value = 1000000 * duration * (float) i / 800;

            //String str = value.toString();
            String string = String.format("%.2f", value);
            graphics.setColor(Color.gray);
            graphics.drawString(string, startX + i - 10, startY + 320);
            graphics.setColor(Color.darkGray);
            graphics.drawLine(startX + i, startY - 300, startX + i, startY + 300);
        }
        graphics.setColor(Color.WHITE);
        if (type == 1) graphics.drawString("voltage [ " + order + "V ]", startX, startY - 310);
        else if (type == 2) graphics.drawString("current [ " + order + "A ]", startX, startY - 310);
        else if (type == 3) graphics.drawString("power [ " + order + "W ]", startX, startY - 310);
        else if (type == 4) graphics.drawString("voltage [ " + order + "V ]", startX, startY - 310);
        graphics.drawLine(startX, startY - 300, startX, startY + 300);

        //X-axis
        for (int i = -300; i <= 300; i += 60) {
            Float value = 0f;
            if (order.equals("k")) value = scale * (float) i / 300 / 1000;
            else if (order.equals("")) value = scale * (float) i / 300;
            else if (order.equals("m")) value = scale * 1000 * (float) i / 300;
            //String str = value.toString();
            String string = String.format("%.2f", value);
            graphics.setColor(Color.gray);
            graphics.drawString(string, startX - 40, startY - i);
            graphics.setColor(Color.darkGray);
            graphics.drawLine(startX, startY - i, startX + 800, startY - i);
        }
        graphics.setColor(Color.WHITE);
        graphics.drawString("time [" + orderOfTime + "s]", startX + 810, startY);
        graphics.drawLine(startX, startY, startX + 800, startY);

        //draw data
        float stepX = 800 / (float) size;
        float stepY = -300 / scale;
        float xp = startX;
        float yp = stepY * list.get(0) + startY;

        if (type == 1) graphics.setColor(Color.green);
        if (type == 2) graphics.setColor(Color.ORANGE);
        if (type == 3) graphics.setColor(Color.RED);
        if (type == 4) graphics.setColor(Color.magenta);
        for (int i = 1; i < size; i++) {
            float y = stepY * list.get(i) + startY;
            graphics.drawLine((int) xp, (int) yp, (int) (xp + stepX), (int) y);
            yp = y;
            xp += stepX;
        }
    }
}
