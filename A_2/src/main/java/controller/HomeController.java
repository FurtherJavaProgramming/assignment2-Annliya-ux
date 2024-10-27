package controller;

// Import necessary JavaFX classes for GUI components and FXML management
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.Model;
import model.Order;
import model.OrderDetail;
import model.User;
import model.UserTableModel;
import model.BookTableModel;
import model.Book;

// Import classes for handling SQL exceptions and file I/O
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dao.OrderDaoImpl;
import javafx.concurrent.Task;

public class HomeController {
    // Fields to manage model data, user and the stage references
    private final Model model;
    private final Stage stage;
    private final Stage parentStage;
    private Order currentOrder;
    private OrderDaoImpl orderDao;
    private final User user; 
    private final UserTableModel userTableModel; 
    private final BookTableModel bookTableModel; 

    // FXML-injected GUI components
    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuItem manageUsers; 
    @FXML
    private MenuItem manageBooks;
    @FXML
    private MenuItem viewProfile; 
    @FXML
    private MenuItem viewBooks; 
    @FXML
    private MenuItem viewOrders;
    @FXML
    private MenuItem viewCart;
    @FXML
    private MenuItem adminViewOrders;
    @FXML
    private MenuItem home;
    @FXML
    private MenuItem adminLogout;
    @FXML
    private MenuItem userLogout;
    @FXML
    private Menu adminMenu; 
    @FXML
    private Menu profileMenu; 
    @FXML
    private Menu actionsMenu; 
    @FXML
    private Label welcomeLabel; 
    @FXML
    private Pane contentArea; 

    private final Label readingRoomLabel;

    // Constructor to initialize HomeController with stage, model, and user details
    public HomeController(Stage parentStage, Model model, User user) {
        this.stage = new Stage();
        this.parentStage = parentStage;
        this.model = model;
        this.user = user; 
        this.userTableModel = new UserTableModel(); 
        this.bookTableModel = new BookTableModel(); 
        this.orderDao = new OrderDaoImpl();
        this.readingRoomLabel = new Label("Reading Room");
        readingRoomLabel.setStyle("-fx-font-size: 24; -fx-text-fill: blue;");
    }

    // Called upon initialization to set up views, menus, and fetch any pending orders
    @FXML
    public void initialize() {
        System.out.println("Initializing HomeController...");
        fetchPendingOrder();
        if (user.isAdmin()) { // Configure UI for admin
            adminMenu.setVisible(true);
            profileMenu.setVisible(false); 
            actionsMenu.setVisible(false); 
            welcomeLabel.setText("Welcome, Admin");
        } else { // Configure UI for regular user
            profileMenu.setVisible(true);
            actionsMenu.setVisible(true);
            adminMenu.setVisible(false); 
            welcomeLabel.setText("Welcome, " + user.getFirstName() + " " + user.getLastName());
            displayTopFiveBooks();
        }
        setupMenuActions(); // Set actions for each menu item
    }

    // Configures each menu item to load appropriate views when clicked
    private void setupMenuActions() {
        manageUsers.setOnAction(event -> loadView("/view/ManageUsersView.fxml"));
        manageBooks.setOnAction(event -> loadView("/view/ManageBooksView.fxml"));
        viewProfile.setOnAction(event -> loadView("/view/ProfileView.fxml"));
        viewBooks.setOnAction(event -> loadView("/view/BookListView.fxml"));
        viewOrders.setOnAction(event -> loadView("/view/OrderView.fxml")); 
        viewCart.setOnAction(event -> loadView("/view/CartView.fxml")); 
        adminViewOrders.setOnAction(event -> loadView("/view/AdminOrderView.fxml")); 
        home.setOnAction(event -> displayTopFiveBooks());
        adminLogout.setOnAction(event -> logout());
        userLogout.setOnAction(event -> logout());   
    }

    // Loads an FXML view based on the provided path and injects appropriate controllers/models
    private void loadView(String fxmlPath) {
        System.out.println("Loading view: " + fxmlPath);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            if ("/view/ManageUsersView.fxml".equals(fxmlPath)) {
                ManageUsersController manageUsersController = loader.getController();
                manageUsersController.setModel(userTableModel);
                System.out.println("Model set for ManageUsersController");
            }

            if ("/view/ManageBooksView.fxml".equals(fxmlPath)) {
                ManageBooksController manageBooksController = loader.getController();
                manageBooksController.setModel(bookTableModel);
                System.out.println("Model set for ManageBooksController");
            }

            if ("/view/ProfileView.fxml".equals(fxmlPath)) {
                ProfileController profileController = loader.getController();
                profileController.setUser(user);
                System.out.println("User set for ProfileController");
            }

            if ("/view/BookListView.fxml".equals(fxmlPath)) {
                BookListController bookListController = loader.getController();
                bookListController.setModel(bookTableModel);
                bookListController.setUser(user);
                System.out.println("Model set for BookListController");
            }

            if ("/view/OrderView.fxml".equals(fxmlPath)) {
                OrderViewController orderController = loader.getController();
                orderController.setUser(user);
                System.out.println("User set for OrderViewController");
            }

            if ("/view/CartView.fxml".equals(fxmlPath)) {
                ShoppingCartController shoppingCartController = loader.getController();
                shoppingCartController.setUser(user);
                System.out.println("User set for ShoppingCartController");
            }

            if ("/view/AdminOrderView.fxml".equals(fxmlPath)) {
                AdminOrderViewController adminOrderController = loader.getController();
                System.out.println("Admin order view controller set up");
            }

            if (contentArea != null) {
                contentArea.getChildren().setAll(view);
                readingRoomLabel.setVisible(false);
            } else {
                System.out.println("contentArea is null when loading view!");
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load the view. Please try again.");
            e.printStackTrace();
        }
    }

    // Logs the user out and shows the login screen
    private void logout() {
        stage.close();
        Stage loginStage = new Stage();
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            LoginController loginController = new LoginController(loginStage, model);
            loader.setController(loginController);
            Pane root = loader.load();
            Scene scene = new Scene(root, 500, 300);
            loginStage.setScene(scene);
            loginStage.setTitle("Login");
            loginStage.setResizable(false);
            loginStage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load the login view. Please try again.");
            e.printStackTrace();
        }
    }

    // Displays the top 5 bestselling books and allows the user to add them to the cart
    private void displayTopFiveBooks() {
        Task<List<Book>> task = new Task<List<Book>>() {
            @Override
            protected List<Book> call() throws Exception {
                return model.getBookDao().getTopFiveBooks();
            }
        };

        task.setOnSucceeded(event -> {
            List<Book> topBooks = task.getValue();
            Pane booksPane = new Pane();
            int yPosition = 10;

            Label headingLabel = new Label("Top 5 Bestselling Books");
            headingLabel.setLayoutX(10);
            headingLabel.setLayoutY(yPosition);
            headingLabel.setStyle("-fx-font-size: 30;");
            booksPane.getChildren().add(headingLabel);
            yPosition += 40;

            for (Book book : topBooks) {
                Label titleLabel = new Label(book.getTitle() + " by " + book.getAuthors());
                titleLabel.setLayoutX(10);
                titleLabel.setLayoutY(yPosition);

                TextField quantityField = new TextField();
                quantityField.setLayoutX(300);
                quantityField.setLayoutY(yPosition);
                quantityField.setPromptText("Qty");
                quantityField.setPrefWidth(50);

                Button addButton = new Button("Add to Cart");
                addButton.setLayoutX(370);
                addButton.setLayoutY(yPosition);
                addButton.setOnAction(e -> {
                    int quantity;
                    try {
                        quantity = Integer.parseInt(quantityField.getText());
                        addToCart(book, quantity);
                    } catch (NumberFormatException ex) {
                        showAlert("Invalid Input", "Please enter a valid number for quantity.");
                    }
                });

                booksPane.getChildren().addAll(titleLabel, quantityField, addButton);
                yPosition += 30;
            }

            contentArea.getChildren().setAll(booksPane);
            readingRoomLabel.setVisible(false);
        });

        task.setOnFailed(event -> {
            System.out.println("Failed to load top books.");
            showAlert("Error", "Failed to load top books. Please try again.");
            event.getSource().getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private void fetchPendingOrder() {
        try {
            List<Order> orders = orderDao.getOrdersByUser(user.getUsername());
            for (Order order : orders) {
                if ("pending".equalsIgnoreCase(order.getStatus())) {
                    currentOrder = order;
                    break;
                }
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to fetch pending orders.");
            e.printStackTrace();
        }
    }

    private void addToCart(Book book, int quantity) {
        if (currentOrder == null) {
            currentOrder = new Order(user.getUsername(), 0.0, "pending", null, new ArrayList<>());
            try {
                orderDao.createOrder(currentOrder);
            } catch (SQLException e) {
                showAlert("Database Error", "Failed to create a new order.");
                e.printStackTrace();
                return;
            }
        }

        for (OrderDetail detail : currentOrder.getOrderDetails()) {
            if (detail.getBookId() == book.getId()) {
                int previousQuantity = detail.getQuantity();
                int newQuantity = previousQuantity + quantity;

                if (newQuantity > book.getPhysicalCopies()) {
                    showAlert("Quantity Exceeded", "You cannot add more than " + book.getPhysicalCopies() + " copies of " + book.getTitle() + " to the cart.");
                    return;
                }

                double previousTotalPrice = book.getPrice() * previousQuantity;
                double newTotalPrice = book.getPrice() * newQuantity;
                double priceDifference = newTotalPrice - previousTotalPrice;

                detail.setQuantity(newQuantity);
                detail.setTotalPrice(newTotalPrice);
                currentOrder.setFinalPrice(currentOrder.getFinalPrice() + priceDifference);

                try {
                    orderDao.updateOrderDetails(currentOrder.getOrderId(), List.of(detail));
                    orderDao.updateOrder(currentOrder);
                    showAlert("Success", quantity + " copies of " + book.getTitle() + " added to cart.");
                } catch (SQLException e) {
                    showAlert("Database Error", "Failed to update the order details.");
                    e.printStackTrace();
                }
                return;
            }
        }

        double totalPrice = book.getPrice() * quantity;
        currentOrder.setFinalPrice(currentOrder.getFinalPrice() + totalPrice);

        OrderDetail orderDetail = new OrderDetail(currentOrder.getOrderId(), book.getId(), quantity, totalPrice);
        currentOrder.getOrderDetails().add(orderDetail);

        try {
            orderDao.createOrderDetails(currentOrder.getOrderId(), List.of(orderDetail));
            orderDao.updateOrder(currentOrder);
            showAlert("Success", quantity + " copies of " + book.getTitle() + " added to cart.");
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to update the order details.");
            e.printStackTrace();
        }

        System.out.println("Added " + quantity + " copies of " + book.getTitle() + " to the cart.");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showStage(Pane root) {
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(root, readingRoomLabel);
        Scene scene = new Scene(stackPane, 600, 500);

        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Reading Room - " + (user.isAdmin() ? "Admin Dashboard" : "User Dashboard"));
        stage.show();
    }
}