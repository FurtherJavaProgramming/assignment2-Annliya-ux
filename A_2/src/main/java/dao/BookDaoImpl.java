package dao;

import model.Book;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDaoImpl implements BookDao {
    private final String TABLE_NAME = "books";

    public BookDaoImpl() {
    }

    @Override
    public void setup() throws SQLException {
        try (Connection connection = Database.getConnection();
             Statement stmt = connection.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + "book_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "title VARCHAR(255) NOT NULL, "
                    + "authors VARCHAR(255) NOT NULL, "
                    + "physical_copies INT NOT NULL, "
                    + "price DOUBLE NOT NULL, "
                    + "sold_copies INT NOT NULL)";
            stmt.executeUpdate(sql);
        }
    }

    @Override
    public void createBook(Book book) throws SQLException {
        String sql = "INSERT INTO " + TABLE_NAME + " (title, authors, physical_copies, price, sold_copies) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthors());
            stmt.setInt(3, book.getPhysicalCopies());
            stmt.setDouble(4, book.getPrice());
            stmt.setInt(5, book.getSoldCopies());

            int affectedRows = stmt.executeUpdate();
            System.out.println("Affected rows: " + affectedRows);

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    book.setId(generatedKeys.getInt(1));
                    System.out.println("Generated book ID: " + book.getId());
                } else {
                    throw new SQLException("Creating book failed, no ID obtained.");
                }
            }
        }
    }

    @Override
    public List<Book> getAllBooks() throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME;
        List<Book> books = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Book book = new Book(
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("authors"),
                    rs.getInt("physical_copies"),
                    rs.getDouble("price"),
                    rs.getInt("sold_copies")
                );
                books.add(book);
            }
        }

        return books;
    }

    @Override
    public void deleteBook(int bookId) throws SQLException {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE book_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void updateBook(Book book) throws SQLException {
        String sql = "UPDATE " + TABLE_NAME + " SET title = ?, authors = ?, physical_copies = ?, price = ?, sold_copies = ? WHERE book_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthors());
            stmt.setInt(3, book.getPhysicalCopies());
            stmt.setDouble(4, book.getPrice());
            stmt.setInt(5, book.getSoldCopies());
            stmt.setInt(6, book.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Book> getTopFiveBooks() throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY sold_copies DESC LIMIT 5";
        List<Book> books = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Book book = new Book(
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("authors"),
                    rs.getInt("physical_copies"),
                    rs.getDouble("price"),
                    rs.getInt("sold_copies")
                );
                books.add(book);
            }
        }
        return books;
    }

    @Override
    public Book getBookById(int bookId) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE book_id = ?";        
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bookId);            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Book(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("authors"),
                        rs.getInt("physical_copies"),
                        rs.getDouble("price"),
                        rs.getInt("sold_copies")
                    );
                }
            }
        }
        return null;
    }

}
