module ru.main.ui_reporter {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires static lombok;
    requires okhttp3;
    requires com.google.gson;

    opens ru.main.ui_reporter to javafx.fxml, com.google.gson;
    exports ru.main.ui_reporter;
}