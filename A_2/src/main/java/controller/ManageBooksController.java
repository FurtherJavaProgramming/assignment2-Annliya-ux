package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.Book;
import model.BookTableModel;

public class ManageBooksController {
    // FXML-injected GUI components
    @FXML
    private TableView<Book> booksTable; // Table view for displaying books
    @FXML
    private TextField titleField; // Input field for book title
    @FXML
    private TextField authorField; // Input field for book author(s)
    @FXML
    private TextField priceField; // Input field for book price
    @FXML
    private TextField physicalCopiesField; // Input field for the number of physical copies
    @FXML
    private TextField soldCopiesField; // Input field for the number of sold copies
    @FXML
    private Button addBookButton; // Button to add a new book
    @FXML
    private Button editBookButton; // Button to edit the selected book
    @FXML
    private Button deleteBookButton; // Button to delete the selected book

    private BookTableModel bookTableModel; // Model for managing the book data

    // Method to set the model and load books from the database
    public void setModel(BookTableModel bookTableModel) {
        this.bookTableModel = bookTableModel;
        bookTableModel.loadBooksFromDatabase(); // Load books into the model
        bookTableModel.setupTableColumns(booksTable); // Set up table columns
        setupSelectionListener(); // Initialize selection listener for the table
    }

    // Initializes the controller and sets up button actions
    @FXML
    public void initialize() {
        addBookButton.setOnAction(event -> addBook());
        editBookButton.setOnAction(event -> editBook());
        deleteBookButton.setOnAction(event -> deleteBook());

        // Restrict input to numbers only for price, physical copies, and sold copies fields
        restrictToNumericInput(priceField, true); // Allows decimal for price
        restrictToNumericInput(physicalCopiesField, false); // Only whole numbers
        restrictToNumericInput(soldCopiesField, false); // Only whole numbers
    }

    // Helper method to restrict input to numeric values
    private void restrictToNumericInput(TextField textField, boolean allowDecimal) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches(allowDecimal ? "\\d*(\\.\\d*)?" : "\\d*")) {
                textField.setText(oldValue); // Revert to old value if invalid input
            }
        });
    }

    // Method to add a new book
    private void addBook() {
        // Retrieve data from input fields
        String title = titleField.getText();
        String authors = authorField.getText();
        double price = Double.parseDouble(priceField.getText());
        int physicalCopies = Integer.parseInt(physicalCopiesField.getText());
        int soldCopies = Integer.parseInt(soldCopiesField.getText());

        // Create a new Book object and add it to the model
        Book newBook = new Book(title, authors, physicalCopies, price, soldCopies);
        bookTableModel.addBook(newBook);
        clearFields(); // Clear input fields after adding
    }

    // Method to edit the selected book
    private void editBook() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem(); // Get selected book
        if (selectedBook != null) {
            // Update selected book's details with input field values
            selectedBook.setTitle(titleField.getText());
            selectedBook.setAuthors(authorField.getText());
            selectedBook.setPrice(Double.parseDouble(priceField.getText()));
            selectedBook.setPhysicalCopies(Integer.parseInt(physicalCopiesField.getText()));
            selectedBook.setSoldCopies(Integer.parseInt(soldCopiesField.getText()));

            try {
                // Update the book in the model
                bookTableModel.updateBook(selectedBook);
            } catch (Exception e) {
                e.printStackTrace(); // Handle any exceptions
            }

            booksTable.refresh(); // Refresh the table view to reflect changes
            clearFields(); // Clear input fields after editing
        }
    }

    // Method to delete the selected book
    private void deleteBook() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem(); // Get selected book
        if (selectedBook != null) {
            bookTableModel.removeBook(selectedBook); // Remove book from the model
            clearFields(); // Clear input fields after deletion
        }
    }

    // Method to clear all input fields
    private void clearFields() {
        titleField.clear();
        authorField.clear();
        priceField.clear();
        physicalCopiesField.clear();
        soldCopiesField.clear();
    }

    // Method to set up a listener for table selection changes
    private void setupSelectionListener() {
        booksTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Populate input fields with the selected book's details
                titleField.setText(newValue.getTitle());
                authorField.setText(newValue.getAuthors());
                priceField.setText(String.valueOf(newValue.getPrice()));
                physicalCopiesField.setText(String.valueOf(newValue.getPhysicalCopies()));
                soldCopiesField.setText(String.valueOf(newValue.getSoldCopies()));
            }
        });
    }
}
