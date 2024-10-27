package dao;

import java.sql.SQLException;
import java.util.List;
import model.User;

public interface UserDao {
    void setup() throws SQLException;
    User getUser(String username, String password) throws SQLException;
    User createUser(String username, String password, String firstName, String lastName) throws SQLException;
    List<User> getAllUsers() throws SQLException;
    void updateUser(User user) throws SQLException;
    User getUserByUsername(String username) throws SQLException; 
}
