package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.User;
import model.UserTableModel;

public class ManageUsersController {
    // FXML-injected GUI components
    @FXML
    private TableView<User> usersTable; // Table view for displaying users
    @FXML
    private TextField firstNameField; // Input field for user's first name
    @FXML
    private TextField lastNameField; // Input field for user's last name
    @FXML
    private TextField emailField; // Input field for user's email (username)
    @FXML
    private Button editUserButton; // Button to edit selected user

    private UserTableModel userTableModel; // Model for managing user data

    // Method to set the model and load users from the database
    public void setModel(UserTableModel userTableModel) {
        this.userTableModel = userTableModel;
        userTableModel.loadUsersFromDatabase(); // Load users into the model
        userTableModel.setupTableColumns(usersTable); // Set up table columns
        setupSelectionListener(); // Initialize selection listener for the table
    }

    // Initializes the controller and sets up button actions
    @FXML
    public void initialize() {
        emailField.setDisable(true); // Disable the email field (username) for editing
        editUserButton.setOnAction(event -> editUser()); // Set action for the edit button
    }

    // Method to edit the selected user
    private void editUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem(); // Get selected user
        if (selectedUser != null) {
            // Retrieve updated information from input fields
            String updatedFirstName = firstNameField.getText();
            String updatedLastName = lastNameField.getText();
            String updatedEmail = emailField.getText();

            // Update selected user's details
            selectedUser.setFirstName(updatedFirstName);
            selectedUser.setLastName(updatedLastName);
            selectedUser.setUsername(updatedEmail); // Assuming username is the email

            try {
                // Update the user in the model
                userTableModel.updateUser(selectedUser);
            } catch (Exception e) {
                e.printStackTrace(); // Handle any exceptions
            }

            usersTable.refresh(); // Refresh the table view to reflect changes
            clearFields(); // Clear input fields after editing
        }
    }

    // Method to clear all input fields
    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
    }

    // Method to set up a listener for table selection changes
    private void setupSelectionListener() {
        usersTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Populate input fields with the selected user's details
                firstNameField.setText(newValue.getFirstName());
                lastNameField.setText(newValue.getLastName());
                emailField.setText(newValue.getUsername()); 
            }
        });
    }
}
