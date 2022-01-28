package ru.mcstyle.printlabels;

import java.util.ArrayList;

public class Shape {
    private final ArrayList<Point> points = new ArrayList<>();

    public void addPoint(double x, double y) {
        points.add(new Point(x, y));
    }

    public int getColPoints() {
        return points.size();
    }

    public Point getPoint(int index) {
        if(index > getColPoints()) return null;
        return points.get(index);
    }
}
