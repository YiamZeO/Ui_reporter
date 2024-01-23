package ru.main.ui_reporter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class ReporterApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ReporterApplication.class.getResource("reporter-view.fxml"));
        Parent root = fxmlLoader.load();
        ReporterController controller = fxmlLoader.getController();
        controller.initHostServices(getHostServices());
        controller.initStage(stage);
        Scene scene = new Scene(root, 515, 620);
        stage.setTitle("Reporter");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.getIcons().add(new Image("file:report_icon.png"));
        stage.show();
        controller.postInitialize();
    }

    public static void main(String[] args) {
        launch();
    }
}