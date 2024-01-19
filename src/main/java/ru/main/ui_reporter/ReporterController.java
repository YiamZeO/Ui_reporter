package ru.main.ui_reporter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.util.*;

public class ReporterController {

    @FXML
    private TextField actionTextField1;

    @FXML
    private TableView<TableEntity> actionsTable;

    @FXML
    private Button changeUserButton;

    @FXML
    private Button deleteActionButton;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private DatePicker endDatePicker1;

    @FXML
    private CheckBox extraHoursCheckBox;

    @FXML
    private Spinner<Integer> hoursSpinner;

    @FXML
    private Button loadReportsButton;

    @FXML
    private TableView<ReportEntity> reportsTable;

    @FXML
    private DatePicker startDatePicker1;

    @FXML
    private Button projectDeleteButton;

    @FXML
    private Button projectSaveButton;

    @FXML
    private TextField projectTextField1;

    @FXML
    private TableView<TableEntity> projectsTable;

    @FXML
    private Button saveActionButton;

    @FXML
    private Button saveUserButton;

    @FXML
    private Button sendReportButton;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private TextField tokenField;

    @FXML
    private TextField userLoginField;

    @FXML
    private Button projectSearchButton;

    @FXML
    private Button searchActionButton;

    @FXML
    private ComboBox<String> projectsBox;

    @FXML
    private ComboBox<String> actionsBox;

    @FXML
    private TextField userPasswordTextField;

    private String userLogin;

    private String userToken;

    private String userPassword;

    private List<TableEntity> projects;

    private List<TableEntity> actions;

    private List<ReportEntity> reportEntities;

    private HostServices hostServices;

    private Stage stage;

    public void initHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    public void initStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    void initialize() {
        hoursSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8));
        startDatePicker.setOnAction(actionEvent -> endDatePickerDisableControl());
        sendReportButton.setOnMouseClicked(mouseEvent -> sendReport());
        changeUserButton.setOnMouseClicked(mouseEvent -> changeUserControl());
        saveUserButton.setOnMouseClicked(mouseEvent -> saveUserControl());
        Gson gson = new Gson();
        try (FileReader reader = new FileReader("user.json")) {
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> userMap = gson.fromJson(reader, type);
            userLogin = userMap.get("userLogin");
            userToken = userMap.get("userToken");
            userPassword = userMap.get("userPassword");
        } catch (IOException e) {
            e.printStackTrace();
        }
        userLoginField.setText(userLogin);
        tokenField.setText(userToken);
        userPasswordTextField.setText(userPassword);

        projectsTable.getColumns().clear();
        TableColumn<TableEntity, Integer> projectNumber = new TableColumn<>("№");
        projectNumber.setCellValueFactory(new PropertyValueFactory<TableEntity, Integer>("number"));
        TableColumn<TableEntity, String> projectName = new TableColumn<>("Добавленные проекты");
        projectName.setCellValueFactory(new PropertyValueFactory<TableEntity, String>("name"));
        projectsTable.getColumns().add(projectNumber);
        projectsTable.getColumns().add(projectName);
        projectsTable.getSelectionModel().setCellSelectionEnabled(true);
        projectsTable.setOnKeyReleased(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.C) {
                TablePosition selectedCell = projectsTable.getFocusModel().getFocusedCell();
                String selectedData = (String) selectedCell.getTableColumn().getCellData(selectedCell.getRow());
                ClipboardContent content = new ClipboardContent();
                content.putString(selectedData);
                Clipboard.getSystemClipboard().setContent(content);
            }
        });

        actionsTable.getColumns().clear();
        TableColumn<TableEntity, Integer> actionNumber = new TableColumn<>("№");
        actionNumber.setCellValueFactory(new PropertyValueFactory<TableEntity, Integer>("number"));
        TableColumn<TableEntity, String> actionName = new TableColumn<>("Добавленные действия");
        actionName.setCellValueFactory(new PropertyValueFactory<TableEntity, String>("name"));
        actionsTable.getColumns().add(actionNumber);
        actionsTable.getColumns().add(actionName);
        actionsTable.getSelectionModel().setCellSelectionEnabled(true);
        actionsTable.setOnKeyReleased(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.C) {
                TablePosition selectedCell = actionsTable.getFocusModel().getFocusedCell();
                String selectedData = (String) selectedCell.getTableColumn().getCellData(selectedCell.getRow());
                ClipboardContent content = new ClipboardContent();
                content.putString(selectedData);
                Clipboard.getSystemClipboard().setContent(content);
            }
        });

        reportsTable.getColumns().clear();
        TableColumn<ReportEntity, LocalDate> reportDate = new TableColumn<>("Дата");
        reportDate.setCellValueFactory(new PropertyValueFactory<ReportEntity, LocalDate>("date"));
        TableColumn<ReportEntity, String> reportProjectName = new TableColumn<>("Проект");
        reportProjectName.setCellValueFactory(new PropertyValueFactory<ReportEntity, String>("projectName"));
        TableColumn<ReportEntity, Long> reportHours = new TableColumn<>("Часы");
        reportHours.setCellValueFactory(new PropertyValueFactory<ReportEntity, Long>("hours"));
                reportsTable.getSelectionModel().setCellSelectionEnabled(true);
        reportsTable.getColumns().addAll(
                reportDate,
                reportProjectName,
                reportHours
        );

        try (FileReader reader = new FileReader("projects.json")) {
            Type type = new TypeToken<List<TableEntity>>() {
            }.getType();
            projects = gson.fromJson(reader, type);
        } catch (Exception e) {
            projects = new ArrayList<>();
            e.printStackTrace();
        }

        try (FileReader reader = new FileReader("actions.json")) {
            Type type = new TypeToken<List<TableEntity>>() {
            }.getType();
            actions = gson.fromJson(reader, type);
        } catch (Exception e) {
            actions = new ArrayList<>();
            e.printStackTrace();
        }

        projectsTable.setItems(FXCollections.observableList(projects));
        actionsTable.setItems(FXCollections.observableList(actions));

        projectSearchButton.setOnMouseClicked(mouseEvent -> searchProjects());
        projectDeleteButton.setOnMouseClicked(mouseEvent -> deleteProjects());
        projectSaveButton.setOnMouseClicked(mouseEvent -> saveProject());

        searchActionButton.setOnMouseClicked(mouseEvent -> searchActions());
        deleteActionButton.setOnMouseClicked(mouseEvent -> deleteActions());
        saveActionButton.setOnMouseClicked(mouseEvent -> saveAction());

        projectsBox.setItems(FXCollections.observableArrayList(projects.stream().map(TableEntity::getName).toList()));
        projectsBox.getEditor().setOnKeyTyped(event -> projectsBoxFiltering());

        actionsBox.setItems(FXCollections.observableArrayList(actions.stream().map(TableEntity::getName).toList()));
        actionsBox.getEditor().setOnKeyTyped(event -> actionsBoxFiltering());

        startDatePicker1.setValue(LocalDate.now().minusDays(7L));
        endDatePicker1.setValue(LocalDate.now());

        loadReportsButton.setOnMouseClicked(mouseEvent -> loadReports());

        reportsTable.setRowFactory(tv -> {
            TableRow<ReportEntity> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                ReportEntity rowData = row.getItem();
                if (!row.isEmpty()){
                    if (event.getButton().equals(MouseButton.PRIMARY)) {
                        String content = new StringBuilder()
                                .append("Дата: ").append(rowData.getDate()).append('\n')
                                .append("Проект: ").append(rowData.getProjectName()).append('\n')
                                .append("Действие: ").append(rowData.getAction()).append('\n')
                                .append("Часы: ").append(rowData.getHours()).append('\n')
                                .append("Описание: ").append(rowData.getDescription()).append('\n')
                                .append("Сверхурочные: ").append(rowData.getExtraHours() ? "Да" : "Нет").append('\n')
                                .append("Id отчета: ").append(rowData.getId())
                                .toString();
                        createAlert(Alert.AlertType.INFORMATION, "Отчет"
                                , "Информация об отчете за " + rowData.getDate(), content, true);
//                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
//                        alert.setTitle("Отчет");
//                        alert.setHeaderText("Информация об отчете за " + rowData.getDate());
//                        String content = new StringBuilder()
//                                .append("Дата: ").append(rowData.getDate()).append('\n')
//                                .append("Проект: ").append(rowData.getProjectName()).append('\n')
//                                .append("Действие: ").append(rowData.getAction()).append('\n')
//                                .append("Часы: ").append(rowData.getHours()).append('\n')
//                                .append("Описание: ").append(rowData.getDescription()).append('\n')
//                                .append("Сверхурочные: ").append(rowData.getExtraHours() ? "Да" : "Нет").append('\n')
//                                .append("Id отчета: ").append(rowData.getId())
//                                .toString();
//                        alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
//                        alert.getDialogPane().getStylesheets().add(
//                                getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
//                        alert.setResizable(true);
//                        TextArea textArea = new TextArea(content);
//                        textArea.setEditable(false);
//                        textArea.setWrapText(true);
//                        VBox reportContent = new VBox(textArea);
//                        alert.getDialogPane().setContent(reportContent);
//                        alert.showAndWait();
                    } else if (event.getButton().equals(MouseButton.SECONDARY)){
                        Optional<ButtonType> result = createAlert(Alert.AlertType.CONFIRMATION, "Отчет",
                                "Перейти к изменению отчета за " + rowData.getDate() + " в Reporter ? ",
                                null, false);
//                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//                        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
//                        alert.setTitle("Отчет");
//                        alert.setHeaderText("Перейти к изменению отчета за " + rowData.getDate() + " в Reporter ? ");
//                        alert.setContentText(null);
//                        alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
//                        alert.getDialogPane().getStylesheets().add(
//                                getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
//                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            hostServices.showDocument("https://reporter.corp.local/report_edit/" + rowData.getId());
                        }
                    }
                }
            });
            return row;
        });
    }

    private void loadReports() {
        if (checkReportsDate()) {
            OkHttpClient client = getDummyClient();
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("token", userToken);
            requestBody.put("employee", userLogin);
            requestBody.put("from", startDatePicker1.getValue().toString());
            requestBody.put("to", endDatePicker1.getValue().toString());
            String apiUrl = "https://reporter.corp.local/api/existed_reports";
            Gson gson = new Gson();
            String json = gson.toJson(requestBody);
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful())
                    throw new RuntimeException("Отчеты не загружены. Причина: " + response.body().string());
                else {
                    Type type = new TypeToken<Map<String, Object>>() {
                    }.getType();
                    assert response.body() != null;
                    Map<String, Object> responseData = gson.fromJson(response.body().string(), type);
                    reportEntities = new ArrayList<>();
                    for (Map<String, Object> report : (List<Map<String, Object>>) responseData.get("reports")) {
                        Optional<TableEntity> project = projects.stream()
                                .filter(p -> Objects.equals(p.getId(), ((Double) report.get("project")).longValue()))
                                .findFirst();
                        Optional<TableEntity> action = actions.stream()
                                .filter(a -> Objects.equals(a.getId(), ((Double) report.get("taskType")).longValue()))
                                .findFirst();
                        reportEntities.add(ReportEntity.builder()
                                .date(LocalDate.parse(((String) report.get("day")).substring(0, 10)))
                                .hours(Double.valueOf((String) report.get("hours")).longValue())
                                .projectName(project.isPresent() ?
                                        project.get().getName() : "Неизвестный проект (нужно добавить)")
                                .action(action.isPresent() ?
                                        action.get().getName() : "Неизвестное действие (нужно добавить)")
                                .description((String) report.get("description"))
                                .id(((Double) report.get("id")).longValue())
                                .extraHours(((Double) report.get("work_after")).intValue() != 0)
                                .build());
                    }
                    reportsTable.setItems(FXCollections.observableList(reportEntities));
                    TableColumn<ReportEntity, ?> date = reportsTable.getColumns().get(0);
                    date.setSortType(TableColumn.SortType.DESCENDING);
                    reportsTable.getSortOrder().clear();
                    reportsTable.getSortOrder().add(date);
                }
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
                alert.setTitle("Ошибка");
                alert.setHeaderText(null);
                alert.setContentText("Отчеты не загружены");
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
                alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
                alert.getDialogPane().getStylesheets().add(
                        getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
                alert.getDialogPane().setExpandableContent(expContent);
                alert.showAndWait();
                throw new RuntimeException(e);
            }
        }
    }

    private boolean checkReportsDate() {
        boolean isNotEmpty = true;
        boolean isDatesCorrect = true;
        if (startDatePicker1.getValue() == null || endDatePicker1.getValue() == null) {
            if (startDatePicker1.getValue() == null)
                startDatePicker1.setStyle("-fx-border-color: #ff0000;");
            else
                startDatePicker1.setStyle("-fx-border-color: transparent;");
            if (endDatePicker1.getValue() == null)
                endDatePicker1.setStyle("-fx-border-color: #ff0000;");
            else
                endDatePicker1.setStyle("-fx-border-color: transparent;");
            isNotEmpty = false;
        } else if (startDatePicker1.getValue().isAfter(endDatePicker1.getValue())) {
            startDatePicker1.setStyle("-fx-border-color: #ff0000;");
            endDatePicker1.setStyle("-fx-border-color: #ff0000;");
            isDatesCorrect = false;
        } else {
            startDatePicker1.setStyle("-fx-border-color: transparent;");
            endDatePicker1.setStyle("-fx-border-color: transparent;");
        }
        if (!isDatesCorrect) {
            createAlert(Alert.AlertType.ERROR, "Ошибка", null,
                    "Даты установлены неправильно", false);
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
//            alert.setTitle("Ошибка");
//            alert.setHeaderText(null);
//            alert.setContentText("Даты установлены неправильно");
//            alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
//            alert.getDialogPane().getStylesheets().add(
//                    getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
//            alert.showAndWait();
        } else if (!isNotEmpty) {
            createAlert(Alert.AlertType.ERROR, "Ошибка", null,
                    "Не все поля заполнены", false);
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
//            alert.setTitle("Ошибка");
//            alert.setHeaderText(null);
//            alert.setContentText("Не все поля заполнены");
//            alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
//            alert.getDialogPane().getStylesheets().add(
//                    getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
//            alert.showAndWait();
        }
        return (isNotEmpty && isDatesCorrect);
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

    private void saveProject() {
        String projectName = projectTextField1.getText();
        if (!projectName.isEmpty()) {
            projects.add(TableEntity.builder()
                    .number(projects.size() + 1)
                    .name(projectName)
                    .id(getProjectId(projectName))
                    .build());
            projectsTable.setItems(FXCollections.observableList(projects));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter("projects.json")) {
                gson.toJson(projects, writer);
            } catch (Exception e) {
                createAlert(Alert.AlertType.ERROR, "Ошибка", null,
                        "Не удалось сохранить проекты пользователя", false);
//                Alert alert = new Alert(Alert.AlertType.ERROR);
//                ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
//                alert.setTitle("Ошибка");
//                alert.setHeaderText(null);
//                alert.setContentText("Не удалось сохранить проекты пользователя");
//                alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
//                alert.getDialogPane().getStylesheets().add(
//                        getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
//                alert.showAndWait();
                throw new RuntimeException(e);
            }
        }
    }

    private void deleteProjects() {
        String projectName = projectTextField1.getText();
        if (!projectName.isEmpty()) {
            List<TableEntity> newProjects = new ArrayList<>();
            for (TableEntity te : projects) {
                if (!Objects.equals(te.getName(), projectName)) {
                    te.setNumber(newProjects.size() + 1);
                    newProjects.add(te);
                }
            }
            projects = newProjects;
            projectsTable.setItems(FXCollections.observableList(projects));
        } else {
            Optional<ButtonType> result = createAlert(Alert.AlertType.CONFIRMATION, "Подтвердите удаление", null,
                    "Будут удалены все проекты. Вы уверены ?", false);
//            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
//            alert.setTitle("Подтвердите удаление");
//            alert.setHeaderText(null);
//            alert.setContentText("Будут удалены все проекты. Вы уверены ?");
//            alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
//            alert.getDialogPane().getStylesheets().add(
//                    getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
//            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                projects.clear();
                projectsTable.setItems(FXCollections.observableList(projects));
            }
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("projects.json")) {
            gson.toJson(projects, writer);
        } catch (Exception e) {
            createAlert(Alert.AlertType.ERROR, "Ошибка", null,
                    "Не удалось сохранить проекты пользователя", false);
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
//            alert.setTitle("Ошибка");
//            alert.setHeaderText(null);
//            alert.setContentText("Не удалось сохранить проекты пользователя");
//            alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
//            alert.getDialogPane().getStylesheets().add(
//                    getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
//            alert.showAndWait();
            throw new RuntimeException(e);
        }
    }

    private void searchProjects() {
        String projectName = projectTextField1.getText();
        if (!projectName.isEmpty())
            projectsTable.setItems(FXCollections.observableList(projects.stream().filter(p -> p.getName().contains(projectName)).toList()));
        else
            projectsTable.setItems(FXCollections.observableList(projects));
    }

    private void saveAction() {
        String actionName = actionTextField1.getText();
        if (!actionName.isEmpty()) {
            actions.add(TableEntity.builder()
                    .number(actions.size() + 1)
                    .name(actionName)
                    .id(getActionId(actionName))
                    .build());
            actionsTable.setItems(FXCollections.observableList(actions));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter("actions.json")) {
                gson.toJson(actions, writer);
            } catch (Exception e) {
                createAlert(Alert.AlertType.ERROR, "Ошибка", null,
                        "Не удалось сохранить действия пользователя", false);
//                Alert alert = new Alert(Alert.AlertType.ERROR);
//                ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
//                alert.setTitle("Ошибка");
//                alert.setHeaderText(null);
//                alert.setContentText("Не удалось сохранить действия пользователя");
//                alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
//                alert.getDialogPane().getStylesheets().add(
//                        getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
//                alert.showAndWait();
                throw new RuntimeException(e);
            }
        }
    }

    private void deleteActions() {
        String actionName = actionTextField1.getText();
        if (!actionName.isEmpty()) {
            List<TableEntity> newActions = new ArrayList<>();
            for (TableEntity te : actions) {
                if (!Objects.equals(te.getName(), actionName)) {
                    te.setNumber(newActions.size() + 1);
                    newActions.add(te);
                }
            }
            actions = newActions;
            actionsTable.setItems(FXCollections.observableList(actions));
        } else {
            Optional<ButtonType> result = createAlert(Alert.AlertType.CONFIRMATION, "Подтвердите удаление", null,
                    "Будут удалены все действия. Вы уверены ?", false);
//            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
//            alert.setTitle("Подтвердите удаление");
//            alert.setHeaderText(null);
//            alert.setContentText("Будут удалены все действия. Вы уверены ?");
//            alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
//            alert.getDialogPane().getStylesheets().add(
//                    getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
//            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                actions.clear();
                actionsTable.setItems(FXCollections.observableList(actions));
            }
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("actions.json")) {
            gson.toJson(actions, writer);
        } catch (Exception e) {
            createAlert(Alert.AlertType.ERROR, "Ошибка", null,
                    "Не удалось сохранить действия пользователя", false);
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
//            alert.setTitle("Ошибка");
//            alert.setHeaderText(null);
//            alert.setContentText("Не удалось сохранить действия пользователя");
//            alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
//            alert.getDialogPane().getStylesheets().add(
//                    getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
//            alert.showAndWait();
            throw new RuntimeException(e);
        }
    }

    private void searchActions() {
        String actionName = actionTextField1.getText();
        if (!actionName.isEmpty())
            actionsTable.setItems(FXCollections.observableList(actions.stream().filter(p -> p.getName().contains(actionName)).toList()));
        else
            actionsTable.setItems(FXCollections.observableList(actions));
    }

    private void changeUserControl() {
        if (Objects.equals(changeUserButton.getText(), "Изменить")) {
            saveUserButton.setDisable(false);
            userLoginField.setDisable(false);
            tokenField.setDisable(false);
            userPasswordTextField.setDisable(false);
            changeUserButton.setText("Отмена");
        } else if (Objects.equals(changeUserButton.getText(), "Отмена")) {
            saveUserButton.setDisable(true);
            userLoginField.setDisable(true);
            tokenField.setDisable(true);
            userPasswordTextField.setDisable(true);
            userLoginField.setText(userLogin);
            tokenField.setText(userToken);
            userPasswordTextField.setText(userPassword);
            userLoginField.setStyle("-fx-border-color: transparent;");
            tokenField.setStyle("-fx-border-color: transparent;");
            changeUserButton.setText("Изменить");
        }
    }

    private void saveUserControl() {
        if (checkUserData()) {
            userLogin = userLoginField.getText();
            userToken = tokenField.getText();
            userPassword = userPasswordTextField.getText();
            Map<String, String> userMap = new HashMap<>();
            userMap.put("userLogin", userLogin);
            userMap.put("userToken", userToken);
            userMap.put("userPassword", userPassword);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter("user.json")) {
                gson.toJson(userMap, writer);
            } catch (IOException e) {
                createAlert(Alert.AlertType.ERROR, "Ошибка", null,
                        "Не удалось сохранить данные пользователя", false);
//                Alert alert = new Alert(Alert.AlertType.ERROR);
//                ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
//                alert.setTitle("Ошибка");
//                alert.setHeaderText(null);
//                alert.setContentText("Не удалось сохранить данные пользователя");
//                alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
//                alert.getDialogPane().getStylesheets().add(
//                        getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
//                alert.showAndWait();
                throw new RuntimeException(e);
            }
            changeUserControl();
        }
    }

    private boolean checkUserData() {
        boolean isNotEmpty = true;
        if (userLoginField.getLength() == 0) {
            userLoginField.setStyle("-fx-border-color: #ff0000;");
            isNotEmpty = false;
        } else {
            userLoginField.setStyle("-fx-border-color: transparent;");
        }
        if (tokenField.getLength() == 0) {
            tokenField.setStyle("-fx-border-color: #ff0000;");
            isNotEmpty = false;
        } else {
            tokenField.setStyle("-fx-border-color: transparent;");
        }
        if (!isNotEmpty) {
            createAlert(Alert.AlertType.ERROR, "Ошибка", null,
                    "Не все поля заполнены", false);
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
//            alert.setTitle("Ошибка");
//            alert.setHeaderText(null);
//            alert.setContentText("Не все поля заполнены");
//            alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
//            alert.getDialogPane().getStylesheets().add(
//                    getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
//            alert.showAndWait();
        }
        return isNotEmpty;
    }

    private void endDatePickerDisableControl() {
        if (startDatePicker.getValue() != null)
            endDatePicker.setDisable(false);
        else {
            endDatePicker.setDisable(true);
            endDatePicker.setValue(null);
        }
    }

    private boolean checkProjectsData() {
        boolean isNotEmpty = true;
        boolean isDatesCorrect = true;
        if (projectsBox.getEditor().getLength() == 0) {
            projectsBox.getEditor().setStyle("-fx-border-color: #ff0000;");
            isNotEmpty = false;
        } else {
            projectsBox.getEditor().setStyle("-fx-border-color: transparent;");
        }
        if (actionsBox.getEditor().getLength() == 0) {
            actionsBox.getEditor().setStyle("-fx-border-color: #ff0000;");
            isNotEmpty = false;
        } else {
            actionsBox.getEditor().setStyle("-fx-border-color: transparent;");
        }
        if (descriptionArea.getLength() == 0) {
            descriptionArea.setStyle("-fx-border-color: #ff0000;");
            isNotEmpty = false;
        } else {
            descriptionArea.setStyle("-fx-border-color: transparent;");
        }
        if (startDatePicker.getValue() == null ||
                (endDatePicker.getValue() != null && (startDatePicker.getValue().isAfter(endDatePicker.getValue())))) {
            startDatePicker.setStyle("-fx-border-color: #ff0000;");
            if (endDatePicker.getValue() != null) {
                endDatePicker.setStyle("-fx-border-color: #ff0000;");
                isDatesCorrect = false;
            }
            isNotEmpty = false;
        } else {
            startDatePicker.setStyle("-fx-border-color: transparent;");
            endDatePicker.setStyle("-fx-border-color: transparent;");
        }
        if (hoursSpinner.getValue() == null) {
            hoursSpinner.setStyle("-fx-border-color: #ff0000;");
            isNotEmpty = false;
        } else {
            hoursSpinner.setStyle("-fx-border-color: transparent;");
        }
        if (!isDatesCorrect) {
            createAlert(Alert.AlertType.ERROR, "Ошибка", null,
                    "Даты установлены неправильно", false);
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
//            alert.setTitle("Ошибка");
//            alert.setHeaderText(null);
//            alert.setContentText("Даты установлены неправильно");
//            alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
//            alert.getDialogPane().getStylesheets().add(
//                    getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
//            alert.showAndWait();
        } else if (!isNotEmpty) {
            createAlert(Alert.AlertType.ERROR, "Ошибка", null,
                    "Не все поля заполнены", false);
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
//            alert.setTitle("Ошибка");
//            alert.setHeaderText(null);
//            alert.setContentText("Не все поля заполнены");
//            alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
//            alert.getDialogPane().getStylesheets().add(
//                    getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
//            alert.showAndWait();
        }
        return (isNotEmpty && isDatesCorrect);
    }

    private Long getActionId(String actionName) {
        OkHttpClient client = getDummyClient();
        HttpUrl apiUrl = Objects.requireNonNull(HttpUrl.parse("https://reporter.corp.local/api/task_types"))
                .newBuilder()
                .addQueryParameter("title", actionName)
                .build();
        Request request = new Request.Builder()
                .url(apiUrl)
                .build();
        try (Response response = client.newCall(request).execute()) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Map<String, Object>>>() {
            }.getType();
            assert response.body() != null;
            List<Map<String, Object>> responseData = gson.fromJson(response.body().string(), type);
            return ((Double) responseData.get(0).get("id")).longValue();
        } catch (Exception e) {
            createAlert(Alert.AlertType.ERROR, "Ошибка", null,
                    "Id для указанного действия не найден. Имя может быть написано некорректно или данное действие не существует",
                    false);
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
//            alert.setTitle("Ошибка");
//            alert.setHeaderText(null);
//            alert.setContentText("Id для указанного действия не найден. Имя может быть написано некорректно или данное действие не существует");
//            alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
//            alert.getDialogPane().getStylesheets().add(
//                    getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
//            alert.showAndWait();
            throw new RuntimeException(e);
        }
    }

    private Long getProjectId(String projectName) {
        OkHttpClient client = getDummyClient();
        HttpUrl apiUrl = Objects.requireNonNull(HttpUrl.parse("https://reporter.corp.local/api/projects"))
                .newBuilder()
                .addQueryParameter("title", projectName)
                .build();
        Request request = new Request.Builder()
                .url(apiUrl)
                .build();
        try (Response response = client.newCall(request).execute()) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Map<String, Object>>>() {
            }.getType();
            assert response.body() != null;
            List<Map<String, Object>> responseData = gson.fromJson(response.body().string(), type);
            return ((Double) responseData.get(0).get("id")).longValue();
        } catch (Exception e) {
            createAlert(Alert.AlertType.ERROR, "Ошибка", null,
                    "Id для указанного проекта не найден. Имя может быть написано некорректно или данный проект не существует",
                    false);
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
//            alert.setTitle("Ошибка");
//            alert.setHeaderText(null);
//            alert.setContentText("Id для указанного проекта не найден. Имя может быть написано некорректно или данный проект не существует");
//            alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
//            alert.getDialogPane().getStylesheets().add(
//                    getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
//            alert.showAndWait();
            throw new RuntimeException(e);
        }
    }

    private OkHttpClient getDummyClient() {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
        return new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                .hostnameVerifier((hostname, session) -> true)
                .build();
    }

    private Optional<ButtonType> createAlert(Alert.AlertType alertType, String title, String header, String content, boolean isTextArea){
        Alert alert = new Alert(alertType);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
        alert.setTitle(title);
        alert.setHeaderText(header);
        if (!isTextArea){
            alert.setContentText(content);
        } else {
            alert.setResizable(true);
            TextArea textArea = new TextArea(content);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            VBox reportContent = new VBox(textArea);
            alert.getDialogPane().setContent(reportContent);
        }
        alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
        return alert.showAndWait();
    }

    private void sendReport() {
        if (checkProjectsData()) {
            OkHttpClient client = getDummyClient();
            Map<String, Object> report = new HashMap<>();
            Optional<TableEntity> project = projects.stream().filter(p -> Objects.equals(p.getName(), projectsBox.getEditor().getText())).findFirst();
            Optional<TableEntity> action = actions.stream().filter(a -> Objects.equals(a.getName(), actionsBox.getEditor().getText())).findFirst();
            report.put("token", userToken);
            report.put("employee", userLogin);
            report.put("description", descriptionArea.getText());
            report.put("hours", hoursSpinner.getValue());
            if (project.isPresent())
                report.put("project", project.get().getId());
            else {
                createAlert(Alert.AlertType.ERROR, "Ошибка", null,
                        "Указанный проект отсутствует в списке доступных проектов",
                        false);
//                Alert alert = new Alert(Alert.AlertType.ERROR);
//                ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
//                alert.setTitle("Ошибка");
//                alert.setHeaderText(null);
//                alert.setContentText("Указанный проект отсутствует в списке доступных проектов");
//                alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
//                alert.getDialogPane().getStylesheets().add(
//                        getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
//                alert.showAndWait();
                throw new RuntimeException("[ERROR] Указанный проект отсутствует в списке доступных проектов");
            }
            if (action.isPresent())
                report.put("taskType", action.get().getId());
            else {
                createAlert(Alert.AlertType.ERROR, "Ошибка", null,
                        "Указанное действие отсутствует в списке доступных действий",
                        false);
//                Alert alert = new Alert(Alert.AlertType.ERROR);
//                ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
//                alert.setTitle("Ошибка");
//                alert.setHeaderText(null);
//                alert.setContentText("Указанное действие отсутствует в списке доступных действий");
//                alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
//                alert.getDialogPane().getStylesheets().add(
//                        getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
//                alert.showAndWait();
                throw new RuntimeException("[ERROR] Указанное действие отсутствует в списке доступных действий");
            }
            report.put("work_after", extraHoursCheckBox.isSelected() ? 1 : 0);
            String apiUrl = "https://reporter.corp.local/api/reports";
            Gson gson = new Gson();
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue() == null ? startDatePicker.getValue() : endDatePicker.getValue();
            for (; start.isBefore(end) || start.isEqual(end); start = start.plusDays(1)) {
                report.put("day", start.toString());
                String json = gson.toJson(report);
                RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
                Request request = new Request.Builder()
                        .url(apiUrl)
                        .post(body)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful())
                        throw new RuntimeException("Отчет: " + report + " не создан. Причина: " + response.body().string());
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
                            getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
                    alert.showAndWait();
                    throw new RuntimeException(e);
                }
            }
            createAlert(Alert.AlertType.INFORMATION, "Успешно", null,
                    "Отчет загружен успешно",
                    false);
//            Alert alert = new Alert(Alert.AlertType.INFORMATION);
//            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
//            alert.setTitle("Успешно");
//            alert.setHeaderText(null);
//            alert.setContentText("Отчет загружен успешно");
//            alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
//            alert.getDialogPane().getStylesheets().add(
//                    getClass().getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
//            alert.showAndWait();
        }
    }
}
