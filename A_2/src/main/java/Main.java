import java.io.IOException;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.fxml.FXMLLoader;

import model.Model;
import controller.LoginController;

public class Main extends Application {
    private Model model;

    // Initializes the application, setting up the model
    @Override
    public void init() {
        model = new Model();
    }

    // Starts the JavaFX application by setting up the main window
    @Override
    public void start(Stage primaryStage) throws SQLException {
        try {
            // Setup the model (possibly establishing database connections, etc.)
            model.setup();

            // Load the Login view from FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));

            // Instantiate the LoginController and associate it with the primary stage and model
            LoginController loginController = new LoginController(primaryStage, model);
            loader.setController(loginController);

            // Load the UI layout (GridPane) from the FXML file
            GridPane root = loader.load();

            // Display the login stage using the loaded layout
            loginController.showStage(root);

        } catch (IOException | RuntimeException e) {
            // Display an error message if an exception occurs during setup
            Scene scene = new Scene(new Label("Error: " + e.getMessage()), 200, 100);
            primaryStage.setTitle("Error");
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    }

    // Main method to launch the JavaFX application
    public static void main(String[] args) {
        launch(args);
    }
}
