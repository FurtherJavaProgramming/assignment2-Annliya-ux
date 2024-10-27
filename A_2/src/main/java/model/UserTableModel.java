package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import dao.UserDaoImpl;
import java.sql.SQLException;
import java.util.List;

public class UserTableModel {
    private ObservableList<User> users;
    private UserDaoImpl userDao;

    public UserTableModel() {
        users = FXCollections.observableArrayList();
        userDao = new UserDaoImpl();
    }

    public ObservableList<User> getUsers() {
        return users;
    }

    public void updateUser(User user) {
        try {
            userDao.updateUser(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearUsers() {
        users.clear();
    }

    public void loadUsersFromDatabase() {
        try {
            List<User> userList = userDao.getAllUsers();
            users.setAll(userList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void setupTableColumns(TableView<User> tableView) {
        tableView.getColumns().clear();

        TableColumn<User, String> idColumn = new TableColumn<>("User ID");
        TableColumn<User, String> firstNameColumn = new TableColumn<>("First Name");
        TableColumn<User, String> lastNameColumn = new TableColumn<>("Last Name");

        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        firstNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName()));
        lastNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastName()));

        tableView.getColumns().addAll(idColumn, firstNameColumn, lastNameColumn);
        tableView.setItems(users);
    }
}
