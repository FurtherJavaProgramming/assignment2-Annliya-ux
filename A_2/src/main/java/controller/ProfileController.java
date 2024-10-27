package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.User;
import dao.UserDao;
import dao.UserDaoImpl;
import java.sql.SQLException;

public class ProfileController {
    @FXML
    private TextField firstNameField; // Field for user's first name
    @FXML
    private TextField lastNameField; // Field for user's last name
    @FXML
    private TextField emailField; // Field for user's email (username)
    @FXML
    private PasswordField passwordField; // Field for user's password
    @FXML
    private Button saveButton; // Button to save changes

    private User user; // Current user object
    private UserDao userDao; // DAO for user operations

    // Constructor to initialize the UserDao implementation
    public ProfileController() {
        this.userDao = new UserDaoImpl(); // Instantiate the UserDao implementation
    }

    // Method to set the current user and populate fields with user data
    public void setUser(User user) {
        this.user = user; // Set the user
        populateFields(); // Populate fields with user data
    }

    // Method to populate the text fields with user information
    private void populateFields() {
        if (user != null) {
            firstNameField.setText(user.getFirstName()); // Set first name field
            lastNameField.setText(user.getLastName()); // Set last name field
            emailField.setText(user.getUsername()); // Set email field
            passwordField.setText(user.getPassword()); // Set password field
        }
    }

    // Initialize method to set up the controller
    @FXML
    private void initialize() {
        emailField.setDisable(true); // Disable editing of the email field
        saveButton.setOnAction(event -> saveChanges()); // Set action for the save button
    }

    // Method to save changes made to the user's profile
    private void saveChanges() {
        // Update user object with data from text fields
        user.setFirstName(firstNameField.getText());
        user.setLastName(lastNameField.getText());
        user.setUsername(emailField.getText());
        user.setPassword(passwordField.getText());

        try {
            userDao.updateUser(user); // Update user in the database
            System.out.println("Profile updated successfully."); // Confirmation message
        } catch (SQLException e) {
            e.printStackTrace(); // Handle SQL exceptions
            System.out.println("Error updating profile."); // Error message
        }
    }
}
