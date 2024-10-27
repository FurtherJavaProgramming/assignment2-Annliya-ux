package dao;

import model.Book;
import model.Order;
import model.OrderDetail;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDaoImpl implements OrderDao {
    private final String MASTER_TABLE_NAME = "orders";
    private final String DETAIL_TABLE_NAME = "orderDetails";

    public OrderDaoImpl() {}

    @Override
    public void setup() throws SQLException {
        try (Connection connection = Database.getConnection();
             Statement stmt = connection.createStatement()) {

            // Create master order table
            String masterSql = "CREATE TABLE IF NOT EXISTS " + MASTER_TABLE_NAME + " (" +
                    "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username VARCHAR(255) NOT NULL, " +
                    "final_price DECIMAL(10, 2) NOT NULL, " +
                    "status VARCHAR(50) NOT NULL, " +
                    "order_datetime TIMESTAMP)";
            stmt.executeUpdate(masterSql);

            // Create order details table
            String detailSql = "CREATE TABLE IF NOT EXISTS " + DETAIL_TABLE_NAME + " (" +
                    "order_id INTEGER NOT NULL, " +
                    "book_id INTEGER NOT NULL, " +
                    "qty INTEGER NOT NULL, " +
                    "total_price DECIMAL(10, 2) NOT NULL, " +
                    "FOREIGN KEY (order_id) REFERENCES " + MASTER_TABLE_NAME + "(order_id), " +
                    "FOREIGN KEY (book_id) REFERENCES books(book_id))";
            stmt.executeUpdate(detailSql);
        }
    }

    @Override
    public void createOrder(Order order) throws SQLException {
        String masterSql = "INSERT INTO " + MASTER_TABLE_NAME + " (username, final_price, status, order_datetime) VALUES (?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement masterStmt = connection.prepareStatement(masterSql, Statement.RETURN_GENERATED_KEYS)) {

            masterStmt.setString(1, order.getUsername());
            masterStmt.setDouble(2, order.getFinalPrice());
            masterStmt.setString(3, order.getStatus());
            masterStmt.setTimestamp(4, new Timestamp(System.currentTimeMillis())); // Set current timestamp for order_datetime
            masterStmt.executeUpdate();

            // Retrieve generated order ID
            try (ResultSet generatedKeys = masterStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);
                    order.setOrderId(orderId); // Set the order ID in the order object
                    createOrderDetails(orderId, order.getOrderDetails());
                }
            }
        }
    }

    public void createOrderDetails(int orderId, List<OrderDetail> orderDetails) throws SQLException {
        String detailSql = "INSERT INTO " + DETAIL_TABLE_NAME + " (order_id, book_id, qty, total_price) VALUES (?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement detailStmt = connection.prepareStatement(detailSql)) {

            for (OrderDetail detail : orderDetails) {
                detailStmt.setInt(1, orderId);
                detailStmt.setInt(2, detail.getBookId());
                detailStmt.setInt(3, detail.getQuantity());
                detailStmt.setDouble(4, detail.getTotalPrice());
                detailStmt.addBatch();
            }
            detailStmt.executeBatch();
        }
    }

    @Override
    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM " + MASTER_TABLE_NAME;
        try (Connection connection = Database.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                String username = rs.getString("username");
                double finalPrice = rs.getDouble("final_price");
                String status = rs.getString("status");
                Timestamp orderDatetime = rs.getTimestamp("order_datetime"); // Fetch the order_datetime

                List<OrderDetail> orderDetails = getOrderDetails(orderId);
                Order order = new Order(username, finalPrice, status, orderDatetime, orderDetails);
                order.setOrderId(orderId);
                order.setOrderDatetime(orderDatetime); // Set the order datetime in the order object
                orders.add(order);
            }
        }
        return orders;
    }

    private List<OrderDetail> getOrderDetails(int orderId) throws SQLException {
        List<OrderDetail> orderDetails = new ArrayList<>();
        String sql = "SELECT * FROM " + DETAIL_TABLE_NAME + " WHERE order_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int bookId = rs.getInt("book_id");
                    int qty = rs.getInt("qty");
                    double totalPrice = rs.getDouble("total_price");

                    OrderDetail detail = new OrderDetail(orderId, bookId, qty, totalPrice);
                    orderDetails.add(detail);
                }
            }
        }
        return orderDetails;
    }

    @Override
    public void updateOrderStatus(int orderId, String status) throws SQLException {
        String sql = "UPDATE " + MASTER_TABLE_NAME + " SET status = ? WHERE order_id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<Order> getOrdersByUser(String username) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM " + MASTER_TABLE_NAME + " WHERE username = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    double finalPrice = rs.getDouble("final_price");
                    String status = rs.getString("status");
                    Timestamp orderDatetime = rs.getTimestamp("order_datetime"); // Fetch the order_datetime

                    List<OrderDetail> orderDetails = getOrderDetails(orderId);
                    Order order = new Order(username, finalPrice, status, orderDatetime, orderDetails);
                    order.setOrderId(orderId);
                    order.setOrderDatetime(orderDatetime); // Set the order datetime in the order object
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    @Override
    public void updateOrder(Order order) throws SQLException {
        String sql = "UPDATE " + MASTER_TABLE_NAME + " SET final_price = ?, status = ? WHERE order_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            // Update the order's final price and status
            pstmt.setDouble(1, order.getFinalPrice());
            pstmt.setString(2, order.getStatus());
            pstmt.setInt(3, order.getOrderId());
            pstmt.executeUpdate();

            // Only update book physical copies if the order is completed
            if ("completed".equalsIgnoreCase(order.getStatus())) {
                List<OrderDetail> orderDetails = getOrderDetails(order.getOrderId());
                for (OrderDetail detail : orderDetails) {
                    updateBookPhysicalCopies(detail.getBookId(), detail.getQuantity());
                }
            }
        }
    }

    @Override
    public void removeOrderDetail(int bookId, int orderId) throws SQLException {
        String sql = "DELETE FROM " + DETAIL_TABLE_NAME + " WHERE book_id = ? AND order_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, bookId); // Set bookId first
            pstmt.setInt(2, orderId); // Then set orderId

            pstmt.executeUpdate();
        }
    }
    
    public void updateBookPhysicalCopies(int bookId, int quantity) throws SQLException {
        String sql = "UPDATE books SET physical_copies = physical_copies - ?,sold_copies = sold_copies + ? WHERE book_id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, quantity);
            pstmt.setInt(2, quantity);
            pstmt.setInt(3, bookId);

            int updatedRows = pstmt.executeUpdate();
            if (updatedRows == 0) {
                throw new SQLException("Failed to update physical copies for book with ID: " + bookId);
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log or handle the exception
            throw new SQLException("Error updating physical copies for book ID " + bookId, e);
        }
    }

    public void updateOrderDetails(int orderId, List<OrderDetail> orderDetails) throws SQLException {
        String sql = "UPDATE " + DETAIL_TABLE_NAME + " SET qty = ?, total_price = ? WHERE order_id = ? AND book_id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            for (OrderDetail detail : orderDetails) {
                pstmt.setInt(1, detail.getQuantity());
                pstmt.setDouble(2, detail.getTotalPrice());
                pstmt.setInt(3, orderId);
                pstmt.setInt(4, detail.getBookId());

                // Execute the update for each order detail
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log or handle the exception as needed
            throw new RuntimeException("Failed to update order details", e);
        }
    }

    public Book getBookById(int bookId) throws SQLException {
        String sql = "SELECT * FROM books WHERE book_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setInt(1, bookId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String title = rs.getString("title");
                    String authors = rs.getString("authors");
                    int physicalCopies = rs.getInt("physical_copies");
                    double price = rs.getDouble("price");
                    int soldCopies = rs.getInt("sold_copies");

                    // Create and return a new Book object
                    return new Book(bookId, title, authors, physicalCopies, price, soldCopies);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Failed to fetch book by ID", e);
        }
        return null; // If no book is found with the given ID
    }

    @Override
    public List<Order> getCompletedOrders(String username) throws SQLException {
        List<Order> completedOrders = new ArrayList<>();
        String sql = "SELECT * FROM " + MASTER_TABLE_NAME + " WHERE username = ? AND status = 'completed'";

        try (Connection connection = Database.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    double finalPrice = rs.getDouble("final_price");
                    String status = rs.getString("status");
                    Timestamp orderDatetime = rs.getTimestamp("order_datetime"); // Fetch the order_datetime

                    List<OrderDetail> orderDetails = getOrderDetails(orderId);
                    Order order = new Order(username, finalPrice, status, orderDatetime, orderDetails);
                    order.setOrderId(orderId);
                    order.setOrderDatetime(orderDatetime); // Set the order datetime in the order object
                    completedOrders.add(order);
                }
            }
        }
        return completedOrders;
    }
    
    @Override
    public List<User> getAllUsers() throws SQLException {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT username, first_name, last_name FROM users WHERE username != 'admin'";
        try (Connection connection = Database.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                User user = new User(); 
                user.setUsername(username);
                user.setFirstName(firstName);
                user.setLastName(lastName);                
                userList.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
            throw new SQLException("Error fetching users from the database", e);
        }
        return userList;
    }
}
