package controller;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import java.sql.Statement;
import javafx.event.ActionEvent;
import emoji.EmojiPicker;
import javafx.scene.control.Button;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class ClientFormController {
    public AnchorPane pane;
    public ScrollPane scrollPain;
    public VBox vBox;
    public TextField txtMsg;
    public Text txtLabel;
    public Button emojiButton;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String clientName = "Client";
    String emailuser=LoginFormController.email;


    public void initialize() {
        txtLabel.setText(clientName);
        this.vBox.setStyle("-fx-background-image: url('/img/loo.png'); " +
                "-fx-background-size: contain;"+"-fx-background-repeat: repeat;");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket("localhost", 3001);
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    System.out.println("Client connected");
                    ServerFormController.receiveMessage(clientName + " joined.");
                    loadOldMessages();


                    while (socket.isConnected()) {
                        String receivingMsg = dataInputStream.readUTF();
                        receiveMessage(receivingMsg, ClientFormController.this.vBox);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        this.vBox.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                scrollPain.setVvalue((Double) newValue);
            }
        });

        emoji();
        menu();

    }

    public void shutdown() {
        ServerFormController.receiveMessage(clientName + " left.");
    }

    private void emoji() {
        // Create the EmojiPicker
        EmojiPicker emojiPicker = new EmojiPicker();

        VBox vBox = new VBox(emojiPicker);
        vBox.setPrefSize(150,300);
        vBox.setLayoutX(400);
        vBox.setLayoutY(175);
        vBox.setStyle("-fx-font-size: 30");

        pane.getChildren().add(vBox);

        // Set the emoji picker as hidden initially
        emojiPicker.setVisible(false);

        // Show the emoji picker when the button is clicked
        emojiButton.setOnAction(event -> {
            if (emojiPicker.isVisible()) {
                emojiPicker.setVisible(false);
            } else {
                emojiPicker.setVisible(true);
            }
        });

        // Set the selected emoji from the picker to the text field
        emojiPicker.getEmojiListView().setOnMouseClicked(event -> {
            String selectedEmoji = emojiPicker.getEmojiListView().getSelectionModel().getSelectedItem();
            if (selectedEmoji != null) {
                txtMsg.setText(txtMsg.getText() + selectedEmoji);
            }
            emojiPicker.setVisible(false);
        });
    }
    private void menu() {
        Image image = new Image("img/menu.png");

        Button imageButton = new Button();
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(35);
        imageView.setFitHeight(35);
        imageButton.setGraphic(imageView);

        VBox vBox = new VBox();
        vBox.setPrefSize(40, 40);
        vBox.setLayoutX(0);
        vBox.setLayoutY(0);
        vBox.setStyle("-fx-font-size: 30; -fx-background-color: #fff; -fx-padding: 50 0 0 0;");
        vBox.setVisible(false);

        pane.getChildren().add(vBox);

        final boolean[] vBoxVisible = {false};

        imageButton.setOnAction(event -> {
            vBoxVisible[0] = !vBoxVisible[0];
            vBox.setVisible(vBoxVisible[0]);
        });

        pane.getChildren().add(imageButton);

        String[] imagePaths = {
                "img/profile.png",
                "img/setting.png",
                "img/logout.png"
        };

        for (int i = 0; i < 3; i++) {
            final int buttonIndex = i;
            Button button = new Button();
            Image buttonImage = new Image(imagePaths[i]);
            ImageView buttonImageView = new ImageView(buttonImage);
            buttonImageView.setFitWidth(35);
            buttonImageView.setFitHeight(35);
            button.setGraphic(buttonImageView);

            button.setOnAction(event -> handleButtonClick(buttonIndex, event));

            vBox.getChildren().add(button);
        }
    }

    private void handleButtonClick(int buttonIndex, ActionEvent event) {
        switch (buttonIndex) {
            case 0:
                openNewStage("/view/Profile.fxml", "Profile");
                break;
            case 1:
                openNewStage("/view/setting.fxml", "Setting");
                break;
            case 2:
                ((Stage) pane.getScene().getWindow()).close();
                break;
        }
    }

    private void openNewStage(String fxmlPath, String title) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(pane.getScene().getWindow());
        try {
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource(fxmlPath))));
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Something went wrong: " + e.getMessage()).show();
        }
        stage.setTitle(title);
        stage.centerOnScreen();
        stage.setResizable(false);
        stage.show();
    }



    public void txtMsgOnAction(ActionEvent actionEvent) {
        sendButtonOnAction(actionEvent);
    }

    public void sendButtonOnAction(ActionEvent actionEvent) {
        sendMsg(txtMsg.getText());
    }

    private void sendMsg(String msgToSend) {
        if (!msgToSend.isEmpty()){
            saveMessageToDatabase(clientName, msgToSend);

            if (!msgToSend.matches(".*\\.(png|jpe?g|gif)$")){

                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER_RIGHT);
                hBox.setPadding(new Insets(2, 2, 0, 5));

                Text text = new Text(msgToSend);
                text.setStyle("-fx-font-size: 14");
                TextFlow textFlow = new TextFlow(text);

//              #0693e3 #37d67a #40bf75
                textFlow.setStyle("-fx-background-color: #0693e3; -fx-font-weight: bold; -fx-color: white; -fx-background-radius: 20px");
                textFlow.setPadding(new Insets(5, 10, 5, 10));
                text.setFill(Color.color(1, 1, 1));

                hBox.getChildren().add(textFlow);

                HBox hBoxTime = new HBox();
                hBoxTime.setAlignment(Pos.CENTER_RIGHT);
                hBoxTime.setPadding(new Insets(0, 5, 5, 10));
                String stringTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                Text time = new Text(stringTime);
                time.setStyle("-fx-font-size: 8");

                hBoxTime.getChildren().add(time);

                vBox.getChildren().add(hBox);
                vBox.getChildren().add(hBoxTime);


                try {

                    System.out.printf(msgToSend+"\n");
                    dataOutputStream.writeUTF(clientName + "-" + msgToSend);
                    dataOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                txtMsg.clear();
            }
        }
    }
    private void saveMessageToDatabase(String senderName, String messageText) {
        // Use try-with-resources to automatically close resources
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/appmessagerie", "root", "");
             PreparedStatement statement = connection.prepareStatement("INSERT INTO messages (email,sender_name, message_text) VALUES (?,?, ?)")) {

            // Set parameters for the prepared statement
            statement.setString(1,emailuser);
            statement.setString(2, senderName);
            statement.setString(3, messageText);

            // Execute the SQL query
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle database errors
        }
    }
    private void loadOldMessages() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/appmessagerie", "root", "");
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM messages WHERE email = ?");
        ) {
            int i=0;
            statement.setString(1, emailuser);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String senderName = resultSet.getString("sender_name");
                String messageText = resultSet.getString("message_text");

                // Utilisez la méthode appropriée pour afficher les messages dans l'interface graphique
                if(i==0)
                {
                receiveMessage(senderName + "-" + messageText, vBox);
                i=i+1;
                }
                else {
                    sendMsg(messageText);
                    i=0;
                }
            }

        } catch (SQLException e) {
            System.out.println("erreur from the data base");
            // Gérez les erreurs de la base de données
        }
    }



    private void sendImage(String imagePath) {
        Image image = new Image(new File(imagePath).toURI().toString());
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(200);
        imageView.setFitWidth(200);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setPadding(new Insets(5, 5, 5, 10));
        hBox.getChildren().add(imageView);

        vBox.getChildren().add(hBox);

        try {
            dataOutputStream.writeUTF(clientName + "-" + imagePath);
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void receiveMessage(String msg, VBox vBox) {
        String[] msgParts = msg.split("[-]");
        String senderName = msgParts[0];

        HBox hBoxName = new HBox();
        hBoxName.setAlignment(Pos.CENTER_LEFT);
        Text textName = new Text(senderName);
        TextFlow textFlowName = new TextFlow(textName);
        hBoxName.getChildren().add(textFlowName);

        if (msg.matches(".*\\.(png|jpe?g|gif)$")) {
            // Image message
            String imagePath = msgParts[1];

            Platform.runLater(() -> {
                try {
                    File file = new File(imagePath);

                    // Check if the file exists
                    if (file.exists()) {
                        String imageUrl = file.toURI().toString();

                        Image image = new Image(imageUrl);
                        ImageView imageView = new ImageView(image);
                        imageView.setFitHeight(200);
                        imageView.setFitWidth(200);

                        HBox hBox = new HBox();
                        hBox.setAlignment(Pos.CENTER_LEFT);
                        hBox.setPadding(new Insets(5, 5, 5, 10));
                        hBox.getChildren().add(imageView);

                        vBox.getChildren().add(hBoxName);
                        vBox.getChildren().add(hBox);
                    } else {
                        System.out.println("Image file not found: " + imagePath);
                    }
                } catch (Exception e) {
                    System.out.println("erreur");;
                }
            });
        } else {
            // Text message
            String msgFromServer = msgParts[1];

            Platform.runLater(() -> {
                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER_LEFT);
                hBox.setPadding(new Insets(5, 5, 5, 10));

                Text text = new Text(msgFromServer);
                TextFlow textFlow = new TextFlow(text);
                textFlow.setStyle("-fx-background-color: #abb8c3; -fx-font-weight: bold; -fx-background-radius: 20px");
                textFlow.setPadding(new Insets(5, 10, 5, 10));
                text.setFill(Color.color(0, 0, 0));

                hBox.getChildren().add(textFlow);

                vBox.getChildren().add(hBoxName);
                vBox.getChildren().add(hBox);
            });
        }
    }





    public void attachedButtonOnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.bmp");
        fileChooser.getExtensionFilters().add(imageFilter);

        // Show the file dialog
        File selectedFile = fileChooser.showOpenDialog(new Stage());

        if (selectedFile != null) {
            // User selected a file, send the image
            String filePath = selectedFile.getAbsolutePath();
            sendImage(filePath);
            System.out.println(filePath + " chosen.");
        }
    }

    public void setClientName(String name) {
        clientName = name;
    }
}
