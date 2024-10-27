package dao;

import model.Order;
import model.User;

import java.sql.SQLException;
import java.util.List;

public interface OrderDao {
    void setup() throws SQLException; 
    void createOrder(Order order) throws SQLException; 
    List<Order> getAllOrders() throws SQLException; 
    void updateOrderStatus(int orderId, String status) throws SQLException; 
    List<Order> getOrdersByUser(String username) throws SQLException; 
	List<Order> getCompletedOrders(String username) throws SQLException;
	void updateOrder(Order order) throws SQLException;
	void removeOrderDetail(int bookid, int orderid) throws SQLException;
	List<User> getAllUsers() throws SQLException;
}
