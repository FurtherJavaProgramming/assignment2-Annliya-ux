package model;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import dao.UserDao;
import dao.UserDaoImpl;
import dao.BookDao;
import dao.BookDaoImpl;
import dao.OrderDao; 
import dao.OrderDaoImpl;

public class Model {
    private final UserDao userDao;
    private final BookDao bookDao;
    private final OrderDao orderDao;
    private User currentUser; 

    public Model() {
        userDao = new UserDaoImpl();
        bookDao = new BookDaoImpl();
        orderDao = new OrderDaoImpl();
    }
    
    public void setup() {
        try {
            userDao.setup();
            bookDao.setup();
            orderDao.setup();

            if (bookDao.getAllBooks().isEmpty()) {
                initializeBooks();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up DAOs", e);
        }
    }

    private void initializeBooks() throws SQLException {
        bookDao.createBook(new Book("Absolute Java", "Savitch", 10, 50, 142));
        bookDao.createBook(new Book("JAVA: How to Program", "Deitel and Deitel", 100, 70, 475));
        bookDao.createBook(new Book("Computing Concepts with JAVA 8 Essentials", "Horstman", 500, 89, 60));
        bookDao.createBook(new Book("Java Software Solutions", "Lewis and Loftus", 500, 99, 12));
        bookDao.createBook(new Book("Java Program Design", "Cohoon and Davidson", 2, 29, 86));
        bookDao.createBook(new Book("Clean Code", "Robert Martin", 100, 45, 300));
        bookDao.createBook(new Book("Gray Hat C#", "Brandon Perry", 300, 68, 178));
        bookDao.createBook(new Book("Python Basics", "David Amos", 1000, 49, 79));
        bookDao.createBook(new Book("Bayesian Statistics The Fun Way", "Will Kurt", 600, 42, 155));
    }

    public UserDao getUserDao() {
        return userDao;
    }
    
    public BookDao getBookDao() {
        return bookDao;
    }

    public OrderDao getOrderDao() {
        return orderDao;
    }
    
    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(this.currentUser);
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void createOrder(double finalPrice, List<OrderDetail> orderDetails) throws SQLException {
        if (currentUser != null) {
            orderDao.createOrder(new Order(currentUser.getUsername(), finalPrice, "Pending", null, orderDetails));
        } else {
            throw new IllegalStateException("No user is logged in.");
        }
    }

    public List<Order> getAllOrders() throws SQLException {
        if (currentUser != null) {
            return orderDao.getOrdersByUser(currentUser.getUsername());
        }
        return List.of();
    }
}
