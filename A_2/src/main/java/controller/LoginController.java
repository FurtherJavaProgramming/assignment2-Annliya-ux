package controller;

import java.io.IOException;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Model;
import model.User;

public class LoginController {
    // FXML-injected GUI components
    @FXML
    private TextField name; // Username input field
    @FXML
    private PasswordField password; // Password input field
    @FXML
    private Label message; // Label for displaying messages to the user
    @FXML
    private Button login; // Login button
    @FXML
    private Button signup; // Signup button

    // Model and Stage references
    private Model model; // Data model
    private Stage stage; // Current stage

    // Constructor to initialize the controller with the stage and model
    public LoginController(Stage stage, Model model) {
        this.stage = stage;
        this.model = model;
    }

    // Initializes the controller; sets up action handlers for buttons
    @FXML
    public void initialize() {
        // Action for the login button
        login.setOnAction(event -> {
            // Check that both fields are not empty
            if (!name.getText().isEmpty() && !password.getText().isEmpty()) {
                User user;
                try {
                    // Attempt to retrieve the user from the model
                    user = model.getUserDao().getUser(name.getText(), password.getText());
                    if (user != null) {
                        // If user is found, set the current user in the model
                        model.setCurrentUser(user);
                        // Load the home view for the user
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeView.fxml"));
                            HomeController homeController = new HomeController(stage, model, user);
                            loader.setController(homeController);
                            VBox root = loader.load();
                            homeController.showStage(root);
                            stage.close(); // Close the login stage
                        } catch (IOException e) {
                            // Display error message if home view fails to load
                            message.setText(e.getMessage());
                            message.setTextFill(Color.RED);
                        }
                    } else {
                        // Display error message for incorrect credentials
                        message.setText("Wrong username or password");
                        message.setTextFill(Color.RED);
                    }
                } catch (SQLException e) {
                    // Display SQL error messages
                    message.setText(e.getMessage());
                    message.setTextFill(Color.RED);
                }
            } else {
                // Notify user of empty input fields
                message.setText("Empty username or password");
                message.setTextFill(Color.RED);
            }

            // Clear the input fields after attempting login
            name.clear();
            password.clear();
        });

        // Action for the signup button
        signup.setOnAction(event -> {
            try {
                // Load the signup view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SignupView.fxml"));
                SignupController signupController = new SignupController(stage, model);
                loader.setController(signupController);
                VBox root = loader.load();
                signupController.showStage(root);
                message.setText(""); // Clear any previous messages
                stage.close(); // Close the login stage
            } catch (IOException e) {
                // Display error message if signup view fails to load
                message.setText(e.getMessage());
                message.setTextFill(Color.RED);
            }
        });
    }

    // Method to display the login stage with the specified root pane
    public void showStage(Pane root) {
        Scene scene = new Scene(root, 500, 300); // Set scene size
        stage.setScene(scene);
        stage.setResizable(false); // Make the stage non-resizable
        stage.setTitle("Welcome"); // Set window title
        stage.show(); // Show the stage
    }
}
