package controller;

import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class InscriptionController {

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private DatePicker birthDatePicker;

    @FXML
    private RadioButton maleRadioButton;

    @FXML
    private RadioButton femaleRadioButton;

    @FXML
    private Button registerButton;
    // This method is called when the register button is clicked
    @FXML
    private void handleRegisterButton() {
        // Get the values from the input fields
        String lastName = lastNameField.getText();
        String firstName = firstNameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String birthDate = (birthDatePicker.getValue() != null) ? birthDatePicker.getValue().toString() : "";
        String gender = maleRadioButton.isSelected() ? "homme" : "femme";

        // Validate input fields
        if (lastName.isEmpty() || firstName.isEmpty() || email.isEmpty() || password.isEmpty() || birthDate.isEmpty()) {
            showErrorAlert("Tous les champs doivent être remplis.");
            return;
        }
        // Validate email format using regex
        String emailRegex = "^[A-Za-z0-9]+[A-Za-z0-9._]*@[A-Za-z]+\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);

        if (!matcher.matches()) {
            showErrorAlert("Format d'e-mail invalide. Utilisez le format *******@***.**");
            return;
        }

        // Check if email is already registered
        if (isEmailRegistered(email)) {
            showErrorAlert("Cet e-mail est déjà enregistré. Veuillez utiliser un autre e-mail.");
            return;
        }

        // Store the values in the database
        insertUserIntoDatabase(lastName, firstName, email, password, birthDate, gender);
    }

    private boolean isEmailRegistered(String email) {
        String url = "jdbc:mysql://localhost:3306/appmessagerie";
        String username = "root";
        String bpassword = "";

        String sql = "SELECT COUNT(*) FROM utilisateurs WHERE email = ?";
        try (Connection connection = DriverManager.getConnection(url, username, bpassword);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void insertUserIntoDatabase(String lastName, String firstName, String email, String password, String birthDate, String gender) {
        // Database connection details
        String url = "jdbc:mysql://localhost:3306/appmessagerie";
        String username = "root";
        String bpassword = "";

        // JDBC variables for opening, closing, and managing connection
        try (Connection connection = DriverManager.getConnection(url, username, bpassword)) {
            // The SQL query to insert data into the 'user' table
            String sql = "INSERT INTO utilisateurs (last_name, first_name, email, password,date_of_birth, gender) VALUES (?, ?, ?, ?, ?, ?)";

            // Use PreparedStatement to prevent SQL injection
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, lastName);
                preparedStatement.setString(2, firstName);
                preparedStatement.setString(3, email);
                preparedStatement.setString(4, password);
                preparedStatement.setString(5, birthDate);
                preparedStatement.setString(6, gender);

                // Execute the update
                preparedStatement.executeUpdate();

                // Afficher un message de succès à l'utilisateur
                Platform.runLater(() -> showInfoAlert("Inscription réussie !"));
                navigateToLoginPage();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Gérer les erreurs de base de données de manière appropriée
            showErrorAlert("Erreur lors de l'inscription. Veuillez réessayer.");
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void navigateToLoginPage() {

        try {
            System.out.println("Navaaaaaaigating to login page...");

            // Charger le fichier FXML de la page de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/LoginForm.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène avec la page de connexion
            Scene scene = new Scene(root);

            // Obtenir le stage actuel à partir de n'importe quel nœud de votre scène
            Stage stage = (Stage) registerButton.getScene().getWindow();

            // Mettre la nouvelle scène sur le stage
            stage.setScene(scene);

            // Afficher le stage mis à jour
            stage.show();

            System.out.println("Navigation successful!");
        } catch (IOException  e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors de la navigation vers la page de connexion.");
        }
    }



    private void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
