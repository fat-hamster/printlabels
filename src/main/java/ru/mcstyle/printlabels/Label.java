package ru.mcstyle.printlabels;

public class Label {
    private final String fileName;
    private final Point coordinates;

    public String getFileName() {
        return fileName;
    }

    public Point getCoordinates() {
        return coordinates;
    }

    public Label(String fileName, Point coordinates) {
        this.fileName = fileName;
        this.coordinates = coordinates;
    }
}
