package controller;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.scene.control.Button;

public class ProfileController {

    @FXML
    private ImageView profileImage;
    @FXML
    private Label FirstNameLabel;

    @FXML
    private Label SecondNameLabel;
    @FXML
    private Label emailLabel;

    @FXML
    private Label genderLabel;
    public void initialize() {
        // Chemin relatif de l'image par rapport au répertoire "src/main/resources/"
        loadUserProfileFromDatabase();
        System.out.println("userName");
    }
    String emailuser=LoginFormController.email;


    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/appmessagerie";
        String username = "root";
        String password = "";
        return DriverManager.getConnection(url, username, password);
    }

    private void loadUserProfileFromDatabase() {
        try (Connection connection = getConnection()) {
            String query = "SELECT * FROM utilisateurs WHERE email = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, emailuser);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String firstName = resultSet.getString("first_name");
                        String lastName = resultSet.getString("last_name");
                        String userGender = resultSet.getString("gender");

                        byte[] imageData = resultSet.getBytes("image");
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageData);
                        Image userImage = new Image(byteArrayInputStream);

                        profileImage.setImage(userImage);
                        FirstNameLabel.setText("First name :"+firstName);
                        SecondNameLabel.setText("Last name :"+lastName);
                        emailLabel.setText("Email :"+emailuser);
                        genderLabel.setText("Gender :"+userGender);
//                        birthDateLabel.setText(birthDate);
                    } else {
                        System.out.println("Utilisateur non trouvé dans la base de données.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleButtonAction(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
