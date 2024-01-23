package ru.main.ui_reporter;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Optional;

public class UtilityClass {
    static public OkHttpClient getDummyClient() {
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

    static public Optional<ButtonType> createAlert(Alert.AlertType alertType, String title, String header,
                                                   String content, boolean isTextArea, Class<?> currentClass) {
        Alert alert = new Alert(alertType);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("file:report_icon.png"));
        alert.setTitle(title);
        alert.setHeaderText(header);
        if (!isTextArea) {
            alert.setContentText(content);
        } else {
            TextArea textArea = new TextArea(content);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            VBox reportContent = new VBox(textArea);
            alert.getDialogPane().setContent(reportContent);
        }
        alert.setResizable(true);
        alert.getDialogPane().setStyle("-fx-font-size: 14px; -fx-background-color: #ffffff;");
        alert.getDialogPane().getStylesheets().add(
                currentClass.getResource("/ru/main/ui_reporter/styles/main.css").toExternalForm());
        return alert.showAndWait();
    }
}
