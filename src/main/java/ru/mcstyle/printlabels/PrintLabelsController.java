package ru.mcstyle.printlabels;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PrintLabelsController {

    @FXML
    private ImageView printImageView;
    @FXML
    private ImageView printAllImageView;
    @FXML
    private AnchorPane canvasAnchorPane;
    @FXML
    private Canvas canvas;
    @FXML
    private ListView<String> labelsList;
    @FXML
    private ListView<String> listFilesView;
    @FXML
    private ImageView openImageview;

    private PrintLabelsApp app;
    private final FileChooser fileChooser = new FileChooser();
    private List<File> files;
    private ArrayList<String> filesName;
    private ArrayList<String> labelsName;
    private final ObservableList<String> listFiles;
    private final ObservableList<String> listLabels;

    private Number currentWidth;
    private Number currentHeight;
    private int idCurrentFile = -1;
    private double maxCanvasY;
    private ChangeListener<String> changeListener;

    public void setApp(PrintLabelsApp app) {
        this.app = app;
    }

    public PrintLabelsController() {
        files = new ArrayList<>();
        filesName = new ArrayList<>();
        labelsName = new ArrayList<>();

        listFiles = FXCollections.observableList(filesName);
        listLabels = FXCollections.observableList(labelsName);

        FileChooser.ExtensionFilter EXTENSION_FILTER = new FileChooser.ExtensionFilter("Plan Files (*.tcn)", "*.tcn");
        fileChooser.getExtensionFilters().setAll(EXTENSION_FILTER);
    }

    public double getdX() {
        return Math.round((currentWidth.doubleValue() / app.getCncFiles().get(0).getWidth()) * 100.0) / 100.0;
    }

    public double getdY() {
        return Math.round((currentHeight.doubleValue() / app.getCncFiles().get(0).getHeight()) * 100.0) / 100.0;
    }

    @FXML
    private void initialize() {
        canvas.setHeight(canvasAnchorPane.getHeight());
        maxCanvasY = canvas.getHeight();
        canvas.setWidth(canvasAnchorPane.getWidth());

        openImageview.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/file_open_icon.png"))));
        printImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/print_icon.png"))));
        printAllImageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/print_all_icon1.png"))));

        listFilesView.setOrientation(Orientation.VERTICAL);
        labelsList.setOrientation(Orientation.VERTICAL);
        listFilesView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        labelsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listFilesView.setItems(listFiles);
        labelsList.setItems(listLabels);

        canvasAnchorPane.heightProperty().addListener((observableValue, oldValue, newValue) -> {
            currentHeight = newValue;
            maxCanvasY = currentHeight.doubleValue();
            canvas.setHeight(currentHeight.doubleValue());
            if(idCurrentFile != -1) {
                printShapes();
            }
        });
        canvasAnchorPane.widthProperty().addListener((observableValue, oldValue, newValue) -> {
            currentWidth = newValue;
            canvas.setWidth(currentWidth.doubleValue());
            if(idCurrentFile != -1) {
                printShapes();
            }
        });
    }

    private int getMaxLengthFileName() {
        ArrayList<Label> labels = app.getCncFiles().get(idCurrentFile).getLabels();
        int size = 0;
        for(Label label: labels) {
            if(size < label.getFileName().length()) {
                size = label.getFileName().length();
            }
        }

        return size;
    }

    private void clearData() {
        if(app.getCncFiles().size() > 0) {
            app.getCncFiles().clear();
        }
    }

    @FXML
    private void handle_open() {
        if(app.getCurrentPath() != null) {
            fileChooser.setInitialDirectory(app.getCurrentPath().toFile());
        }
        files = fileChooser.showOpenMultipleDialog(app.getPrimaryStage());
        if(files == null)
            return;

        app.setCurrentPath(files.get(0).toPath().getParent());

        clearData();

        idCurrentFile = 0;
        app.loadFiles(files);

        showFilesView();
        setListener();
        showLabelsView();
        printShapes();
    }

    private void chooseFile(String name) {

        for (int i = 0; i < app.getCncFiles().size(); i++) {
            if(name.contains(app.getCncFiles().get(i).getFileName())) {
                idCurrentFile = i;
                break;
            }
        }

        showLabelsView();
        printShapes();
    }

    private void showLabelsView() {
        labelsName = app.getCncFiles().get(idCurrentFile).getLabelsName();
        listLabels.clear();
        listLabels.setAll(labelsName);
        labelsList.setPrefWidth(getMaxLengthFileName() + 30);
    }

    private void showFilesView() {
        deleteListener();
        listFiles.clear();
        filesName = app.getFilesName();
        listFiles.setAll(filesName);

        listFilesView.getSelectionModel().select(idCurrentFile);
        listFilesView.getFocusModel().focus(idCurrentFile);
    }

    private void deleteListener() {
        if(changeListener != null) {
            listFilesView.getSelectionModel().selectedItemProperty().removeListener(changeListener);
        }
    }

    private void setListener() {
        deleteListener();

        listFilesView.getSelectionModel().selectedItemProperty().addListener(changeListener = (observableValue, s, t1) -> chooseFile(t1));
    }

    private void printShapes() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.GREEN);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for(Shape shape: app.getCncFiles().get(idCurrentFile).getShapes()) {
            drawShapes(gc, shape);
        }
    }

    private void drawShapes(GraphicsContext gc, Shape shape) {
        int offsetX = 1;
        int offsetY = 3;
        double x1 = shape.getPoint(0).getX() * getdX();
        double y1 = maxCanvasY - shape.getPoint(0).getY() * getdY();

        for(int i = 1; i < shape.getColPoints(); i++) {
            double x2 = shape.getPoint(i).getX() * getdX();
            double y2 = maxCanvasY - shape.getPoint(i).getY() * getdY();
            gc.strokeLine(x1 + offsetX, y1 + offsetY, x2 + offsetX, y2 + offsetY);
            x1 = x2;
            y1 = y2;
        }
    }


    @FXML
    private void handle_print() {

    }

    @FXML
    private void handle_printAll() {

    }

    @FXML
    private void handle_exit() {
        System.exit(0);
    }
}