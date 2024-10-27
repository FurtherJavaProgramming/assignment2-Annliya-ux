package model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import dao.BookDaoImpl;
import java.sql.SQLException;
import java.util.List;

public class BookTableModel {
    private ObservableList<Book> books;
    private BookDaoImpl bookDao;

    public BookTableModel() {
        books = FXCollections.observableArrayList();
        bookDao = new BookDaoImpl();
        loadBooksFromDatabase();
    }

    public ObservableList<Book> getBooks() {
        return books;
    }

    public void addBook(Book book) {
        books.add(book);
        try {
            bookDao.createBook(book);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateBook(Book book) {
        try {
            bookDao.updateBook(book);
            loadBooksFromDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeBook(Book book) {
        books.remove(book);
        try {
            bookDao.deleteBook(book.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadBooksFromDatabase() {
        try {
            List<Book> bookList = bookDao.getAllBooks();
            books.setAll(bookList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void setupTableColumns(TableView<Book> tableView) {
        tableView.getColumns().clear();

        TableColumn<Book, Integer> idColumn = new TableColumn<>("ID");
        TableColumn<Book, String> titleColumn = new TableColumn<>("Title");
        TableColumn<Book, String> authorsColumn = new TableColumn<>("Authors");
        TableColumn<Book, String> priceColumn = new TableColumn<>("Price (AUD)");
        TableColumn<Book, String> copiesColumn = new TableColumn<>("Physical Copies");
        TableColumn<Book, Integer> soldCopiesColumn = new TableColumn<>("Sold Copies");

        idColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        authorsColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthors()));
        priceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getPrice())));
        copiesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getPhysicalCopies())));
        soldCopiesColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getSoldCopies()).asObject());

        tableView.getColumns().addAll(idColumn, titleColumn, authorsColumn, priceColumn, copiesColumn, soldCopiesColumn);
        tableView.setItems(books);
    }
}
