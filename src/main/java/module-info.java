module ru.mcstyle.printlabels {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens ru.mcstyle.printlabels to javafx.fxml;
    exports ru.mcstyle.printlabels;
}