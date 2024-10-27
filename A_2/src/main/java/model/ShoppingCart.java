package model;

import java.util.HashMap;
import java.util.Map;

public class ShoppingCart {
    private final Map<Book, Integer> cartItems = new HashMap<>();

    public void addBook(Book book, int quantity) {
        cartItems.put(book, cartItems.getOrDefault(book, 0) + quantity);
    }

    public Map<Book, Integer> getCartItems() {
        return cartItems;
    }
    
    public void clearCart() {
        cartItems.clear();
    }
}
