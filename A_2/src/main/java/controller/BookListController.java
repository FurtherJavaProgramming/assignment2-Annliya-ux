package controller;

import dao.OrderDaoImpl;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import model.Book;
import model.BookTableModel;
import model.Order;
import model.OrderDetail;
import model.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookListController {

    @FXML
    private Pane booksPane;

    private BookTableModel bookTableModel;
    private OrderDaoImpl orderDao;
    private List<OrderDetail> shoppingCart;
    private Order currentOrder;
    private User user;

    // Constructor initializing order DAO and shopping cart list
    public BookListController() {
        orderDao = new OrderDaoImpl();
        shoppingCart = new ArrayList<>();
    }

    // Set the user and fetch any pending orders
    public void setUser(User user) {
        this.user = user;
        fetchPendingOrder();
    }

    // Retrieve the user's pending order from the database if it exists
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

    // Sets the book data model and displays books in the view
    public void setModel(BookTableModel model) {
        this.bookTableModel = model;
        displayBooks();
    }

    // Display the list of books in the view
    private void displayBooks() {
        if (bookTableModel != null) {
            List<Book> books = bookTableModel.getBooks();
            booksPane.getChildren().clear();

            Label headingLabel = new Label("All Available Books");
            headingLabel.setStyle("-fx-font-size: 30; -fx-font-weight: bold;");
            headingLabel.setLayoutX(10);
            headingLabel.setLayoutY(10);
            booksPane.getChildren().add(headingLabel);

            int yPosition = 50;

            if (books.isEmpty()) {
                Label noBooksLabel = new Label("No books available.");
                noBooksLabel.setLayoutX(10);
                noBooksLabel.setLayoutY(yPosition);
                booksPane.getChildren().add(noBooksLabel);
            } else {
                int index = 1;
                for (Book book : books) {
                    Label titleLabel = new Label(index + ". " + book.getTitle() + " by " + book.getAuthors());
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
                            if (quantity < 1) {
                                showAlert("Invalid Quantity", "Quantity must be greater than 0.");
                            } else if (quantity > book.getPhysicalCopies()) {
                                showAlert("Quantity Exceeded", "You cannot add more than " + book.getPhysicalCopies() + " copies of " + book.getTitle() + " to the cart.");
                            } else {
                                addToCart(book, quantity);
                            }
                        } catch (NumberFormatException ex) {
                            showAlert("Invalid Input", "Please enter a valid number for quantity.");
                        }
                    });

                    booksPane.getChildren().addAll(titleLabel, quantityField, addButton);
                    yPosition += 30;
                    index++;
                }
            }
        } else {
            Label errorLabel = new Label("Book model is not set.");
            errorLabel.setLayoutX(10);
            errorLabel.setLayoutY(50);
            booksPane.getChildren().add(errorLabel);
        }
    }

    // Adds a book to the shopping cart or updates the existing quantity if already added
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

    // Displays an alert message with the given title and message
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
