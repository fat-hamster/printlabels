package ru.mcstyle.printlabels;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PrintLabelsApp extends Application {

    private final ArrayList<LoadCNCFile> cncFiles = new ArrayList<>();
    private Stage primaryStage;
    private Path currentPath;
    private List<File> files = new ArrayList<>();
    private final ArrayList<String> filesName = new ArrayList<>();

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        FXMLLoader fxmlLoader = new FXMLLoader(PrintLabelsApp.class.getResource("print_labels-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        primaryStage.setTitle("PrintLabelsApp");
        primaryStage.setScene(scene);

        PrintLabelsController controller = fxmlLoader.getController();
        controller.setApp(this);

        currentPath = null;
        primaryStage.show();
    }

    public ArrayList<String> getFilesName() {
        filesName.clear();
        for(File file: files) {
            filesName.add(file.getName());
        }

        return filesName;
    }

    public ArrayList<LoadCNCFile> getCncFiles() {
        return cncFiles;
    }

    public Path getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(Path path) {
        currentPath = path;
    }

    public void loadFiles(List<File> files) {
        this.files = files;
        for(File file: files) {
            cncFiles.add(new LoadCNCFile(file.toPath()));
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch();
    }
}