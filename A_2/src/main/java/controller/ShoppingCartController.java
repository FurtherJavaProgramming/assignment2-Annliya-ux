package controller;

import dao.OrderDaoImpl;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.control.Dialog;
import model.Order;
import model.OrderDetail;
import model.User;
import model.Book;

import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

public class ShoppingCartController {

    @FXML
    private Pane cartPane; // Pane to display cart items

    @FXML
    private Button checkoutButton; // Button to initiate checkout

    private OrderDaoImpl orderDao; // DAO implementation for order operations
    private Order currentOrder; // Current order being processed
    private User user; // Current user

    private static final int ITEM_SPACING = 30; // Spacing for items in the cart

    // Constructor initializes the OrderDao
    public ShoppingCartController() {
        orderDao = new OrderDaoImpl(); // Instantiate OrderDao
    }

    // Sets the current user and fetches their pending order
    public void setUser(User user) {
        this.user = user; 
        fetchPendingOrder(); // Load pending order
    }

    // Fetches the user's pending order from the database
    private void fetchPendingOrder() {
        try {
            List<Order> orders = orderDao.getOrdersByUser(user.getUsername()); // Get orders for user
            currentOrder = orders.stream()
                    .filter(order -> "pending".equalsIgnoreCase(order.getStatus())) // Find pending order
                    .findFirst()
                    .orElse(null);

            if (currentOrder == null) {
                System.out.println("No pending order found for user: " + user.getUsername());
            }

            displayCartItems(); // Display items in the cart
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to fetch pending orders."); // Show alert on error
            e.printStackTrace();
        }
    }

    // Displays items in the cart
    private void displayCartItems() throws SQLException {
        cartPane.getChildren().clear(); // Clear existing items

        if (currentOrder == null || currentOrder.getOrderDetails().isEmpty()) {
            showEmptyCartMessage(); // Show empty cart message if no items
            return;
        }

        checkoutButton.setVisible(true); // Show checkout button
        int yPosition = 10; // Initial Y position for item display
        boolean disableCheckout = false; // Flag for checkout availability

        // Iterate through order details to display items
        for (OrderDetail detail : currentOrder.getOrderDetails()) {
            Book book = orderDao.getBookById(detail.getBookId()); // Fetch book details

            if (book != null) {
                disableCheckout = displayCartItem(detail, book, yPosition, disableCheckout);
                yPosition += ITEM_SPACING; // Increment Y position for next item
            } else {
                System.out.println("Book not found for ID: " + detail.getBookId());
            }
        }

        Label totalPriceLabel = new Label("Total: $" + currentOrder.getFinalPrice()); // Show total price
        totalPriceLabel.setLayoutX(10);
        totalPriceLabel.setLayoutY(yPosition + 20);
        cartPane.getChildren().add(totalPriceLabel); // Add total price label to pane

        checkoutButton.setDisable(disableCheckout); // Disable checkout if needed
    }

    // Displays a message when the cart is empty
    private void showEmptyCartMessage() {
        Label noItemsLabel = new Label("Your cart is empty.");
        noItemsLabel.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5; -fx-padding: 30;"); // Style empty message
        noItemsLabel.setLayoutX(10);
        noItemsLabel.setLayoutY(10);
        cartPane.getChildren().add(noItemsLabel); // Add message to pane
        checkoutButton.setVisible(false); // Hide checkout button
    }

    // Displays a single cart item
    private boolean displayCartItem(OrderDetail detail, Book book, int yPosition, boolean disableCheckout) {
        String itemText = detail.getQuantity() + " x " + book.getTitle() + " by " + book.getAuthors() + " - $" + detail.getTotalPrice() + " AUD";

        TextFlow itemFlow = new TextFlow();
        itemFlow.setLayoutX(10);
        itemFlow.setLayoutY(yPosition);
        itemFlow.setPrefWidth(350);
        itemFlow.setStyle("-fx-padding: 5;");

        Text itemLabel = new Text(itemText);
        itemLabel.setStyle("-fx-font-weight: normal; -fx-fill: black;");

        // Check stock availability and display warning if necessary
        if (detail.getQuantity() > book.getPhysicalCopies()) {
            disableCheckout = true; // Disable checkout if stock is insufficient
            Text stockWarningLabel = createStockWarningLabel(book);
            itemFlow.getChildren().addAll(itemLabel, stockWarningLabel); // Add warning to display
        } else {
            itemFlow.getChildren().add(itemLabel); // Add item label to display
        }

        cartPane.getChildren().add(itemFlow); // Add item to pane

        Button removeButton = new Button("Remove"); // Button to remove item from cart
        removeButton.setLayoutX(400);
        removeButton.setLayoutY(yPosition);
        cartPane.getChildren().add(removeButton); // Add remove button to pane

        // Set action for the remove button
        removeButton.setOnAction(event -> {
            try {
                removeItemFromCart(detail); // Remove item from cart
            } catch (SQLException e) {
                e.printStackTrace(); // Handle SQL exceptions
            }
        });

        return disableCheckout; // Return checkout availability status
    }

    // Creates a warning label for insufficient stock
    private Text createStockWarningLabel(Book book) {
        Text stockWarningLabel;
        if (book.getPhysicalCopies() == 0) {
            stockWarningLabel = new Text(" (Sold Out)");
            stockWarningLabel.setStyle("-fx-font-weight: bold; -fx-fill: red;"); // Style sold out warning
        } else {
            stockWarningLabel = new Text(" (Only " + book.getPhysicalCopies() + " copies available)");
            stockWarningLabel.setStyle("-fx-font-weight: bold; -fx-fill: orange;"); // Style low stock warning
        }
        return stockWarningLabel; // Return stock warning label
    }

    // Removes an item from the cart and updates the order
    private void removeItemFromCart(OrderDetail detail) throws SQLException {
        currentOrder.getOrderDetails().remove(detail); // Remove item from order details

        double updatedPrice = currentOrder.getFinalPrice() - detail.getTotalPrice(); // Update total price
        currentOrder.setFinalPrice(updatedPrice);

        // Remove order detail from database
        orderDao.removeOrderDetail(detail.getBookId(), detail.getOrderId());
        orderDao.updateOrder(currentOrder); // Update order in the database

        displayCartItems(); // Refresh displayed cart items
    }

    // Initiates the checkout process
    @FXML
    private void onCheckout() {
        if (currentOrder == null || currentOrder.getOrderDetails().isEmpty()) {
            showAlert("Empty Cart", "Your cart is empty. Add items before proceeding to checkout.");
        } else if (checkoutButton.isDisabled()) {
            showAlert("Checkout Unavailable", "Some items in your cart exceed available stock. Please adjust quantities or remove items marked as Sold Out.");
        } else {
            showPaymentDialog(); // Show payment dialog if everything is valid
        }
    }

    // Displays the payment confirmation dialog
    private void showPaymentDialog() {
        Dialog<ButtonType> paymentDialog = new Dialog<>();
        paymentDialog.setTitle("Payment Confirmation");

        // Create fields for card information
        TextField cardNumberField = new TextField();
        cardNumberField.setPromptText("Card Number (16 digits)");
        
        TextField expiryDateField = new TextField();
        expiryDateField.setPromptText("Expiry Date (MM/YY)");
        
        TextField cvvField = new TextField();
        cvvField.setPromptText("CVV (3 digits)");

        String totalPrice = currentOrder != null ? String.format("Total Price: $%.2f", currentOrder.getFinalPrice()) : "$0.00";
        Label priceLabel = new Label(totalPrice); // Show total price in dialog

        javafx.scene.layout.VBox dialogPaneContent = new javafx.scene.layout.VBox(10, priceLabel, cardNumberField, expiryDateField, cvvField);
        paymentDialog.getDialogPane().setContent(dialogPaneContent); // Set dialog content
        
        paymentDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL); // Add buttons to dialog
        
        // Handle dialog result
        paymentDialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String cardNumber = cardNumberField.getText();
                String expiryDate = expiryDateField.getText();
                String cvv = cvvField.getText();
                if (validatePayment(cardNumber, expiryDate, cvv)) { // Validate payment details
                    completeCheckout(); // Complete the checkout process
                }
            }
            return null;
        });

        paymentDialog.showAndWait(); // Show dialog and wait for user action
    }

    // Validates payment information
    private boolean validatePayment(String cardNumber, String expiryDate, String cvv) {
        if (cardNumber.length() != 16 || !cardNumber.matches("\\d+")) {
            showAlert("Invalid Card Number", "Card number must be 16 digits.");
            return false; // Invalid card number
        }

        String[] expiryParts = expiryDate.split("/");
        if (expiryParts.length != 2) {
            showAlert("Invalid Expiry Date", "Expiry date must be in MM/YY format.");
            return false; // Invalid expiry date format
        }

        try {
            int month = Integer.parseInt(expiryParts[0]);
            int year = Integer.parseInt(expiryParts[1]);

            if (month < 1 || month > 12) {
                showAlert("Invalid Expiry Date", "Month must be between 01 and 12.");
                return false; // Invalid month
            }

            LocalDate now = LocalDate.now();
            LocalDate expiryDateParsed = LocalDate.of(2000 + year, month, 1); // Parse expiry date

            if (expiryDateParsed.isBefore(now)) {
                showAlert("Expired Card", "The card has expired.");
                return false; // Card has expired
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Expiry Date", "Expiry date must be in MM/YY format.");
            return false; // Non-numeric expiry date
        } catch (DateTimeException e) {
            showAlert("Invalid Expiry Date", "The expiry date is invalid.");
            return false; // Invalid date format
        }

        if (cvv.length() != 3 || !cvv.matches("\\d+")) {
            showAlert("Invalid CVV", "CVV must be 3 digits.");
            return false; // Invalid CVV
        }

        return true; // All validations passed
    }

    // Completes the checkout process
    private void completeCheckout() {
        try {
            orderDao.updateOrderStatus(currentOrder.getOrderId(), "completed"); // Update order status
            showAlert("Checkout Successful", "Your order has been placed successfully!");
            currentOrder = null; // Reset current order
            showEmptyCartMessage(); // Show empty cart message
            fetchPendingOrder(); // Reload pending order
            displayCartItems(); // Refresh cart items
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to complete checkout."); // Show alert on error
            e.printStackTrace();
        }
    }

    // Displays an alert dialog
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message); // Set alert message
        alert.showAndWait(); // Show alert and wait for user action
    }
}
