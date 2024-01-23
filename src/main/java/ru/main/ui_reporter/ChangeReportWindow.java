package ru.main.ui_reporter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeReportWindow extends Application {
    private ChangeReportController changeReportController;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(ReporterApplication.class.getResource("reporter-update-window.fxml"));
        Parent root = fxmlLoader.load();
        changeReportController = fxmlLoader.getController();
        changeReportController.setCurrentStage(stage);
        Scene scene = new Scene(root, 515, 424);
        stage.setTitle("Reporter");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.getIcons().add(new Image("file:report_icon.png"));
        stage.show();
    }
}
