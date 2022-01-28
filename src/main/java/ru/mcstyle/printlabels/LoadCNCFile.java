package ru.mcstyle.printlabels;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LoadCNCFile {

    private ArrayList<Shape> shapes;
    private List<String> fileBody;
    private ArrayList<Label> labels;
    private int startPosition = 0;
    private double height = 0;
    private double width = 0;
    private String fileName;

    public LoadCNCFile(Path fileName) {
        try {
            this.fileName = fileName.getFileName().toString();
            fileBody = Files.readAllLines(fileName, StandardCharsets.UTF_16);
            shapes = new ArrayList<>();
            labels = new ArrayList<>();
            if(!parse()) {
                throw new MyException("Ошибка обработки файла!");
            }
        } catch(IOException | MyException e) {
            System.out.println("Беда :(");
            System.out.println(fileName);
            System.out.println(e.getMessage());
        }
    }

    public ArrayList<Label> getLabels() {
        return labels;
    }

    public ArrayList<String> getLabelsName() {
        ArrayList<String> names = new ArrayList<>();
        for(Label label: labels) {
            names.add(label.getFileName());
        }

        return names;
    }

    public String getFileName() {
        return fileName;
    }

    public double getHeight() {
        return height;
    }
    public double getWidth() {
        return width;
    }

    private double getCoordinate(String source, String pattern) {
        int start = source.indexOf(pattern) + pattern.length();
        if (start == 0)
            return -1.0;
        String forParse = source.substring(start, source.indexOf(' ', start));
        double res;
        try {
            res = Double.parseDouble(forParse);
        } catch (NumberFormatException e) {
            System.out.printf("String for parse: %s\n", forParse);
            System.out.println(e.getMessage());
            return -1;
        }
        return res;
    }

    public boolean parse() {
        if(fileBody == null)
            return false;
        if(!fileBody.get(1).contains("$=Nesting Result")) {
            System.out.println("Странный файл какой-то...");
            return false;
        }
        for (String s : fileBody) {
            if(s.contains("::UNm")) {
                int start = s.indexOf("DL=") + 3;
                String w = s.substring(start, s.indexOf('.', start + 3));
                start = s.indexOf("DH=") + 3;
                String h = s.substring(start, s.indexOf('.', start + 3));

                try {
                    height = Double.parseDouble(h);
                    width = Double.parseDouble(w);
                } catch (NumberFormatException e) {
                    System.out.println("Беда :(");
                    System.out.println(e.getMessage());
                }
                break;
            }
        }

        // Чистим файл
        for (int i = 0; i < fileBody.size(); i++) {
            if(fileBody.get(i).contains("W#1057")) {
                startPosition = i;
                break;
            }
        }

        // Парсим этикетки
        for(; startPosition < fileBody.size(); startPosition++) {
            if(!fileBody.get(startPosition).contains("W#1057")) break;
            String currentLine = fileBody.get(startPosition);
            int start = currentLine.indexOf("#8507=") + "#8507=".length();
            int end = currentLine.indexOf(" #", start);

            String currentName = currentLine.substring(start, end);
            double x = getCoordinate(currentLine, "#8021=");
            double y = getCoordinate(currentLine, "#8020=");
            labels.add(new Label(currentName, new Point(x, y)));
        }

        // Парсим контуры
        int idx = 0;
        for(int i = startPosition; i < fileBody.size(); i++) {
            String currentLine = fileBody.get(i);
            if(currentLine.contains("W#89")) {
                shapes.add(new Shape());
                shapes.get(idx).addPoint(getCoordinate(currentLine, "#1=a;"), getCoordinate(currentLine, "#2=a;"));
                i++;
                for(; i < fileBody.size(); i++) {
                    currentLine = fileBody.get(i);
                    if(currentLine.contains("W#2201") || currentLine.contains("W#2101")) {
                        shapes.get(idx).addPoint(getCoordinate(currentLine, "#1=a;"), getCoordinate(currentLine, "#2=a;"));
                    } else {
                        idx++;
                        i--;
                        break;
                    }
                }
            }
        }
        return true;
    }

    public ArrayList<Shape> getShapes() {
        return shapes;
    }

    private static class MyException extends Exception {
        public MyException(String s) {
            super(s);
        }
    }
}
