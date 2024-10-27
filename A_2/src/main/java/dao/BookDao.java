package dao;

import model.Book;

import java.sql.SQLException;
import java.util.List;

public interface BookDao {
    void setup() throws SQLException; 
    void createBook(Book book) throws SQLException; 
    List<Book> getAllBooks() throws SQLException; 
    void updateBook(Book book) throws SQLException; 
	List<Book> getTopFiveBooks() throws SQLException;
	void deleteBook(int bookId) throws SQLException;
	Book getBookById(int bookId) throws SQLException;
}
