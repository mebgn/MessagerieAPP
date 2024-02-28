package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;

import javafx.scene.control.ButtonType;
import javafx.scene.Parent;


import javafx.stage.Stage;

import javafx.scene.control.Button;



import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginFormController {

    @FXML
    private TextField emailTextField;
    @FXML
    private PasswordField passTextField;
     static String email;
    @FXML
    private Button CreateAnAccount;

    //  the database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/appmessagerie";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public void initialize() {
    }

    @FXML
    public void logInButtonOnAction(ActionEvent actionEvent) throws IOException {
        email = emailTextField.getText();
        String password = passTextField.getText();


        if (!email.isEmpty() && !password.isEmpty() && verifyCredentials(email, password)) {
            Stage primaryStage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("../view/ClientForm.fxml"));

            ClientFormController controller = new ClientFormController();
            controller.setClientName(email); // Set the parameter
            fxmlLoader.setController(controller);

            primaryStage.setScene(new Scene(fxmlLoader.load()));
            primaryStage.setTitle(email);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.setOnCloseRequest(windowEvent -> {
                controller.shutdown();
            });
            primaryStage.show();

            emailTextField.clear();
        }  else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid email or password\nPlease check your credentials and try again.", ButtonType.OK);
            alert.setHeaderText("Login Failed");
            alert.showAndWait();
        }

    }

    private boolean verifyCredentials(String email, String password) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT * FROM utilisateurs WHERE email = ? AND password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, email);
                preparedStatement.setString(2, password);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next(); // Return true if there is a matching user
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void handleCreateAccount() {
        System.out.println("my test");
        navigateToRegistrationPage();
    }

    private void navigateToRegistrationPage() {
        try {
            System.out.println("Navigating to registration page...");

            // Charger le fichier FXML de la page d'inscription
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/inscription.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène avec la page d'inscription
            Scene scene = new Scene(root);

            // Obtenir le stage actuel à partir de n'importe quel nœud de votre scène
            Stage stage = (Stage) CreateAnAccount.getScene().getWindow();

            // Mettre la nouvelle scène sur le stage
            stage.setScene(scene);

            // Afficher le stage mis à jour
            stage.show();

            System.out.println("Navigation successful!");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors de la navigation vers la page d'inscription.");
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

