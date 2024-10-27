package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
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

public class OrderViewController {

    @FXML
    private VBox ordersContainer; // Container for displaying orders

    @FXML
    private ScrollPane scrollPane; // Scrollable area for the order display

    private OrderDaoImpl orderDao; // DAO for handling order data
    private BookDaoImpl bookDao; // DAO for handling book data
    private User user; // The current user

    // Initializes the controller and sets up DAOs
    public void initialize() {
        orderDao = new OrderDaoImpl(); // Instantiate the order DAO
        bookDao = new BookDaoImpl(); // Instantiate the book DAO
        scrollPane.setPrefHeight(300); // Set preferred height for the scroll pane
        scrollPane.setPrefWidth(500); // Set preferred width for the scroll pane
    }

    // Sets the current user and loads their completed orders
    public void setUser(User user) {
        this.user = user;
        loadCompletedOrders(); // Load orders for the given user
    }

    // Loads completed orders for the current user
    private void loadCompletedOrders() {
        if (user == null) {
            System.out.println("User is not set!"); // Check if the user is set
            return;
        }

        try {
            // Fetch completed orders from the DAO
            List<Order> completedOrders = orderDao.getCompletedOrders(user.getUsername());

            // Check if there are no orders and display a message
            if (completedOrders.isEmpty()) {
                Label noOrdersLabel = createWrappedLabel("No completed orders found.");
                ordersContainer.getChildren().add(noOrdersLabel);
            } else {
                // Iterate over orders and create a UI representation for each
                for (Order order : completedOrders) {
                    VBox orderBox = createOrderBox(order);
                    ordersContainer.getChildren().add(orderBox);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle SQL exceptions
        }
    }

    // Creates a VBox to represent an individual order
    private VBox createOrderBox(Order order) {
        VBox orderBox = new VBox(10); // Vertical box with spacing
        orderBox.setPadding(new javafx.geometry.Insets(10)); // Padding for the box
        orderBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5; -fx-padding: 10;"); // Styling for the order box
        orderBox.setMaxWidth(450); // Set maximum width

        // Create labels for order details
        Label orderIdLabel = createWrappedLabel("Order ID: " + order.getOrderId());
        Label datetimeLabel = createWrappedLabel("Date and Time: " + order.getOrderDatetime());
        Label finalPriceLabel = createWrappedLabel("Final Price: " + order.getFinalPrice() + " AUD");
        Label statusLabel = createWrappedLabel("Status: " + order.getStatus());

        // Add labels to the order box
        orderBox.getChildren().addAll(orderIdLabel, datetimeLabel, finalPriceLabel, statusLabel);

        // Create and add the order details box
        VBox orderDetailsBox = createOrderDetailsBox(order.getOrderDetails());
        orderBox.getChildren().add(orderDetailsBox);

        return orderBox; // Return the constructed order box
    }

    // Creates a VBox to display order details
    private VBox createOrderDetailsBox(List<OrderDetail> orderDetails) {
        VBox detailsBox = new VBox(5); // Vertical box with spacing
        Label detailsHeading = new Label("Order Details:");
        detailsHeading.setStyle("-fx-font-weight: bold; -fx-underline: true;"); // Style for the heading
        detailsBox.getChildren().add(detailsHeading); // Add heading to the details box

        // Iterate over each order detail
        for (OrderDetail detail : orderDetails) {
            try {
                // Fetch the book associated with the order detail
                Book book = bookDao.getBookById(detail.getBookId());
                // Create a string with book details
                String bookDetailText = "Title: " + book.getTitle() +
                                        ", Author: " + book.getAuthors() +
                                        ", Qty: " + detail.getQuantity() +
                                        ", Subtotal: " + detail.getTotalPrice() + " AUD";
                Label bookDetailLabel = createWrappedLabel(bookDetailText); // Create label for the book detail
                bookDetailLabel.setStyle("-fx-padding: 0 0 0 10;"); // Add padding to the label
                detailsBox.getChildren().add(bookDetailLabel); // Add label to the details box

            } catch (SQLException e) {
                e.printStackTrace(); // Handle SQL exceptions
            }
        }

        return detailsBox; // Return the constructed details box
    }

    // Helper method to create a wrapped label
    private Label createWrappedLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true); // Enable text wrapping
        label.setMaxWidth(450); // Set maximum width for the label
        return label; // Return the created label
    }

    // Export orders to a CSV file
    @FXML
    private void exportOrders() {
        if (user == null) {
            System.out.println("User is not set!"); // Check if the user is set
            return;
        }

        // Open a file chooser for the user to select a save location
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Order Details");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv")); // Filter for CSV files
        File file = fileChooser.showSaveDialog(scrollPane.getScene().getWindow());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write the CSV header
                writer.write("Order ID,Date and Time,Final Price,Status,Book Title,Author,Quantity,Subtotal\n");

                // Fetch completed orders again to export
                List<Order> completedOrders = orderDao.getCompletedOrders(user.getUsername());

                // Iterate over each order and write details to the CSV
                for (Order order : completedOrders) {
                    for (OrderDetail detail : order.getOrderDetails()) {
                        Book book = bookDao.getBookById(detail.getBookId()); // Get book details
                        writer.write(order.getOrderId() + "," +
                                     order.getOrderDatetime() + "," +
                                     order.getFinalPrice() + "," +
                                     order.getStatus() + "," +
                                     book.getTitle() + "," +
                                     book.getAuthors() + "," +
                                     detail.getQuantity() + "," +
                                     detail.getTotalPrice() + "\n"); // Write order and book details to the file
                    }
                }

                System.out.println("Order details exported successfully!"); // Confirmation message

            } catch (IOException | SQLException e) {
                e.printStackTrace(); // Handle IO and SQL exceptions
            }
        }
    }
}
