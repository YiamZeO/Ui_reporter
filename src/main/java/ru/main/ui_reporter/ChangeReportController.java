package ru.main.ui_reporter;

import com.google.gson.Gson;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import lombok.Data;
import okhttp3.*;

import java.net.URL;
import java.util.*;

@Data
public class ChangeReportController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ComboBox<String> actionsBox;

    @FXML
    private Button canselButton;

    @FXML
    private Button changeReportButton;

    @FXML
    private Button deleteReportButton;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Spinner<Double> hoursSpinner;

    @FXML
    private ComboBox<String> projectsBox;

    private Stage currentStage;

    private String userLogin;

    private String userToken;

    private List<TableEntity> projects;

    private List<TableEntity> actions;

    private ReportEntity currentReportEntity;

    private HostServices hostServices;

    private ReporterController reporterController;

    @FXML
    private TextField reportDate;

    @FXML
    void initialize() {
    }

    public void postInitialize() {
        reportDate.setText(currentReportEntity.getDate().toString());

        hoursSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 8, currentReportEntity.getHours(), 0.5));

        projectsBox.setItems(FXCollections.observableArrayList(projects.stream().map(TableEntity::getName).toList()));
        projectsBox.getEditor().setOnKeyTyped(event -> projectsBoxFiltering());
        projectsBox.getEditor().setText(currentReportEntity.getProjectName());

        actionsBox.setItems(FXCollections.observableArrayList(actions.stream().map(TableEntity::getName).toList()));
        actionsBox.getEditor().setOnKeyTyped(event -> actionsBoxFiltering());
        actionsBox.getEditor().setText(currentReportEntity.getAction());

        descriptionArea.setText(currentReportEntity.getDescription());

        canselButton.setOnMouseClicked(mouseEvent -> currentStage.close());

        deleteReportButton.setOnMouseClicked(mouseEvent -> {
            hostServices.showDocument("https://reporter.corp.local/report_delete/" + currentReportEntity.getId());
            currentStage.close();
            reporterController.loadReports();
        });

        changeReportButton.setOnMouseClicked(mouseEvent -> {
            changeReport();
            currentStage.close();
            reporterController.loadReports();
        });
    }

    private void changeReport() {
        OkHttpClient client = UtilityClass.getDummyClient();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("token", userToken);
        requestBody.put("day", currentReportEntity.getDate().toString());
        requestBody.put("description", descriptionArea.getText());
        requestBody.put("hours", hoursSpinner.getValue());
        requestBody.put("employee", userLogin);
        Optional<TableEntity> project = projects.stream().filter(p -> Objects.equals(p.getName(), projectsBox.getEditor().getText())).findFirst();
        Optional<TableEntity> action = actions.stream().filter(a -> Objects.equals(a.getName(), actionsBox.getEditor().getText())).findFirst();
        if (project.isPresent())
            requestBody.put("project", project.get().getId());
        else {
            UtilityClass.createAlert(Alert.AlertType.ERROR, "Ошибка", null,
                    "Указанный проект отсутствует в списке доступных проектов",
                    false, this.getClass());
            throw new RuntimeException("[ERROR] Указанный проект отсутствует в списке доступных проектов");
        }
        if (action.isPresent())
            requestBody.put("taskType", action.get().getId());
        else {
            UtilityClass.createAlert(Alert.AlertType.ERROR, "Ошибка", null,
                    "Указанное действие отсутствует в списке доступных действий",
                    false, this.getClass());
            throw new RuntimeException("[ERROR] Указанное действие отсутствует в списке доступных действий");
        }
        String apiUrl = "https://reporter.corp.local/api/reports/" + currentReportEntity.getId();
        Gson gson = new Gson();
        String json = gson.toJson(requestBody);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(apiUrl)
                .put(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful())
                throw new RuntimeException("Отчет: " + requestBody + " не создан. Причина: " + Objects.requireNonNull(response.body()).string());
            response.close();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText("Отчет не создан или создан частично");
            TextArea textArea = new TextArea(e.getMessage());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);
            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(textArea, 0, 1);
            alert.getDialogPane().setExpandableContent(expContent);
            alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
            alert.getDialogPane().getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/ru/main/ui_reporter/styles/main.css")).toExternalForm());
            alert.setResizable(true);
            alert.showAndWait();
            throw new RuntimeException(e);
        }
        UtilityClass.createAlert(Alert.AlertType.INFORMATION, "Успешно", null,
                "Отчет изменен успешно",
                false, this.getClass());
    }

    private void projectsBoxFiltering() {
        List<String> results = projects.stream().map(TableEntity::getName)
                .filter(name -> name.contains(projectsBox.getEditor().getText())).toList();
        projectsBox.setItems(FXCollections.observableArrayList(results));
        projectsBox.show();
    }

    private void actionsBoxFiltering() {
        List<String> results = actions.stream().map(TableEntity::getName)
                .filter(name -> name.contains(actionsBox.getEditor().getText())).toList();
        actionsBox.setItems(FXCollections.observableArrayList(results));
        actionsBox.show();
    }
}

