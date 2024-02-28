package controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;


import org.controlsfx.control.textfield.CustomPasswordField;
import org.controlsfx.control.textfield.CustomTextField;

public class SettingController {

    @FXML
    private ImageView profileImage;
    @FXML
    private CustomTextField firstName;
    @FXML
    private CustomTextField lastName;

    @FXML
    private CustomPasswordField Password;
    String emailuser=LoginFormController.email;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/appmessagerie";

    @FXML
    private void handleImportButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.gif"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            // Charger l'image sélectionnée et l'afficher dans l'ImageView
            Image image = new Image(selectedFile.toURI().toString());
            profileImage.setImage(image);
        }
    }
    public void initialize() throws SQLException {
        loadUserProfileFromDatabase();
        System.out.println("userName");
    }
    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/appmessagerie";
        String username = "root";
        String password = "";
        return DriverManager.getConnection(url, username, password);
    }
    @FXML
    void handleUpdateButtonClick(ActionEvent event) throws SQLException {
        String prenom = firstName.getText();
        String nom = lastName.getText();
        String mot_de_passe = Password.getText();
        Image image = profileImage.getImage();
        byte[] imageData = imageToByteArray(image);

        if (prenom.isEmpty() || nom.isEmpty() || mot_de_passe.isEmpty()|| imageData.length==0) {
            showAlert(AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }



        try (Connection connection = getConnection()) {
            String query = "UPDATE utilisateurs SET first_name=?, last_name=?, password=?,image=? WHERE email=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(5, emailuser);
                preparedStatement.setString(1, prenom);
                preparedStatement.setString(2, nom);
                preparedStatement.setString(3, mot_de_passe);
                preparedStatement.setBytes(4, imageData);

                System.out.println(prenom);

                preparedStatement.executeUpdate();

                // Afficher une boîte de dialogue pour indiquer que la modification est effectuée
                showAlert(AlertType.INFORMATION, "Modification réussie", "La modification a été effectuée avec succès.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void loadUserProfileFromDatabase() throws SQLException {
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM utilisateurs WHERE email= ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, emailuser);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String firstname = resultSet.getString("first_name");
                        String lastname = resultSet.getString("last_name");

                        // Check if imageData is null before creating ByteArrayInputStream
                        byte[] imageData = resultSet.getBytes("image");
                        if (imageData != null) {
                            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageData);
                            Image userImage = new Image(byteArrayInputStream);

                            profileImage.setImage(userImage);
                        }

                        firstName.setText(firstname);
                        lastName.setText(lastname);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Méthode pour afficher une boîte de dialogue
    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private byte[] imageToByteArray(Image image) {
        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    @FXML
    private void handleButtonAction(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

}

