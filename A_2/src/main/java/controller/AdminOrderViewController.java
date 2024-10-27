package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import model.Order;
import model.OrderDetail;
import model.Book;
import model.User;
import dao.OrderDaoImpl;
import dao.BookDaoImpl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AdminOrderViewController {

    @FXML
    private VBox ordersContainer;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Button exportButton;

    private OrderDaoImpl orderDao;
    private BookDaoImpl bookDao;

    // Initializes the controller, setting up DAOs and loading orders
    public void initialize() {
        orderDao = new OrderDaoImpl();
        bookDao = new BookDaoImpl();
        scrollPane.setPrefHeight(300);
        scrollPane.setPrefWidth(500);
        loadAllOrders();
    }

    // Loads all completed orders and displays them in the ordersContainer
    private void loadAllOrders() {
        try {
            // Retrieve all users from the database
            List<User> users = orderDao.getAllUsers();
            for (User user : users) {
                // Get completed orders for each user
                List<Order> userOrders = orderDao.getCompletedOrders(user.getUsername());

                // Display a message if no completed orders are found
                if (userOrders.isEmpty()) {
                    Label noOrdersLabel = createWrappedLabel("No completed orders found for " + user.getFirstName() + " " + user.getLastName());
                    ordersContainer.getChildren().add(noOrdersLabel);
                } else {
                    // Display the user's completed orders
                    VBox userOrdersBox = createUserOrdersBox(user, userOrders);
                    ordersContainer.getChildren().add(userOrdersBox);
                }
            }

            // Apply CSS and layout updates
            ordersContainer.applyCss();
            ordersContainer.layout();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Creates a VBox to display each user's completed orders
    private VBox createUserOrdersBox(User user, List<Order> orders) {
        VBox userOrdersBox = new VBox(10);
        userOrdersBox.setStyle("-fx-padding: 10; -fx-border-color: lightblue; -fx-border-radius: 5;");

        // Label for user information
        Label userLabel = createWrappedLabel("User: " + user.getFirstName() + " " + user.getLastName() + " (" + user.getUsername() + ")");
        userOrdersBox.getChildren().add(userLabel);

        // Add each order to the user's order box
        for (Order order : orders) {
            VBox orderBox = createOrderBox(order);
            userOrdersBox.getChildren().add(orderBox);
        }

        return userOrdersBox;
    }

    // Creates a VBox for displaying a single order's details
    private VBox createOrderBox(Order order) {
        VBox orderBox = new VBox(10);
        orderBox.setPadding(new javafx.geometry.Insets(10));
        orderBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5; -fx-padding: 10;");
        orderBox.setMaxWidth(450);

        // Labels for order details
        Label orderIdLabel = createWrappedLabel("Order ID: " + order.getOrderId());
        Label datetimeLabel = createWrappedLabel("Date and Time: " + order.getOrderDatetime());
        Label finalPriceLabel = createWrappedLabel("Final Price: " + order.getFinalPrice() + " AUD");
        Label statusLabel = createWrappedLabel("Status: " + order.getStatus());

        // Add order details to the order box
        orderBox.getChildren().addAll(orderIdLabel, datetimeLabel, finalPriceLabel, statusLabel);

        // Add order item details to the order box
        VBox orderDetailsBox = createOrderDetailsBox(order.getOrderDetails());
        orderBox.getChildren().add(orderDetailsBox);

        return orderBox;
    }

    // Creates a VBox for displaying each item within an order
    private VBox createOrderDetailsBox(List<OrderDetail> orderDetails) {
        VBox detailsBox = new VBox(5);
        Label detailsHeading = createWrappedLabel("Order Details:");
        detailsHeading.setStyle("-fx-font-weight: bold; -fx-underline: true;");
        detailsBox.getChildren().add(detailsHeading);

        // For each order detail, retrieve and display book information
        for (OrderDetail detail : orderDetails) {
            try {
                Book book = bookDao.getBookById(detail.getBookId());
                String bookDetailText = "Title: " + book.getTitle() + 
                                        ", Author: " + book.getAuthors() + 
                                        ", Qty: " + detail.getQuantity() + 
                                        ", Subtotal: " + detail.getTotalPrice() + " AUD";
                Label bookDetailLabel = createWrappedLabel(bookDetailText);
                bookDetailLabel.setStyle("-fx-padding: 0 0 0 10;");
                detailsBox.getChildren().add(bookDetailLabel);
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return detailsBox;
    }

    // Exports order details to a CSV file selected by the user
    @FXML
    private void exportOrders() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Order Details");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(scrollPane.getScene().getWindow());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write CSV header
                writer.write("Username,Order ID,Date and Time,Final Price,Status,Book Title,Author,Quantity,Subtotal\n");

                // Write details for each user's completed orders
                List<User> users = orderDao.getAllUsers();
                for (User user : users) {
                    List<Order> userOrders = orderDao.getCompletedOrders(user.getUsername());
                    for (Order order : userOrders) {
                        for (OrderDetail detail : order.getOrderDetails()) {
                            Book book = bookDao.getBookById(detail.getBookId());
                            writer.write(user.getUsername() + "," +
                                         order.getOrderId() + "," +
                                         order.getOrderDatetime() + "," +
                                         order.getFinalPrice() + "," +
                                         order.getStatus() + "," +
                                         book.getTitle() + "," +
                                         book.getAuthors() + "," +
                                         detail.getQuantity() + "," +
                                         detail.getTotalPrice() + "\n");
                        }
                    }
                }

                System.out.println("Order details exported successfully!");

            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Helper method to create a wrapped label with specified text
    private Label createWrappedLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(450);
        return label;
    }
}
