package controller;

import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Model;
import model.User;

public class SignupController {
    @FXML
    private TextField first_name; // Input field for the user's first name
    @FXML
    private TextField last_name; // Input field for the user's last name
    @FXML
    private TextField username; // Input field for the user's username
    @FXML
    private TextField password; // Input field for the user's password
    @FXML
    private Button createUser; // Button to trigger user creation
    @FXML
    private Button close; // Button to close the signup window
    @FXML
    private Label status; // Label to display status messages

    private Stage stage; // Stage for the signup window
    private Stage parentStage; // Reference to the parent stage (main window)
    private Model model; // Model instance for data access

    // Constructor for the SignupController
    public SignupController(Stage parentStage, Model model) {
        this.stage = new Stage(); // Create a new stage for signup
        this.parentStage = parentStage; // Store the reference to the parent stage
        this.model = model; // Store the model instance
    }

    // Initialize method to set up event handlers
    @FXML
    public void initialize() {
        // Event handler for the createUser button
        createUser.setOnAction(event -> {
            // Check that all fields are filled
            if (!first_name.getText().isEmpty() && !last_name.getText().isEmpty() 
                && !username.getText().isEmpty() && !password.getText().isEmpty()) {
                
                User user; // Variable to store the created user
                try {
                    // Create a new user via the model's user DAO
                    user = model.getUserDao().createUser(
                            username.getText(), 
                            password.getText(), 
                            first_name.getText(), 
                            last_name.getText()
                    );
                    
                    // Check if the user was created successfully
                    if (user != null) {
                        status.setText("Created " + user.getUsername()); // Show success message
                        status.setTextFill(Color.GREEN); // Set text color to green
                    } else {
                        status.setText("Cannot create user"); // Show error message
                        status.setTextFill(Color.RED); // Set text color to red
                    }
                } catch (SQLException e) {
                    status.setText(e.getMessage()); // Display SQL error message
                    status.setTextFill(Color.RED); // Set text color to red
                }
            } else {
                // Inform the user to fill in all fields if any are empty
                status.setText("Please fill in all fields");
                status.setTextFill(Color.RED); // Set text color to red
            }
        });

        // Event handler for the close button
        close.setOnAction(event -> {
            stage.close(); // Close the signup stage
            parentStage.show(); // Show the parent stage again
        });
    }

    // Method to display the signup stage
    public void showStage(Pane root) {
        Scene scene = new Scene(root, 500, 400); // Create a new scene
        stage.setScene(scene); // Set the scene to the stage
        stage.setResizable(false); // Make the stage non-resizable
        stage.setTitle("Sign up"); // Set the title of the stage
        stage.show(); // Show the signup stage
    }
}
