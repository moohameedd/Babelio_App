package com.example.tp;

import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static DataManager instance;
    private final List<Book> favorites = new ArrayList<>();
    private final List<Book> cart      = new ArrayList<>();

    public static synchronized DataManager getInstance() {
        if (instance == null) instance = new DataManager();
        return instance;
    }

    /* ---- Favorites ---- */
    public void addFavorite(Book book) {
        if (!favorites.contains(book)) {
            favorites.add(book);
            NotificationActivity.addNotification("Added to Favorites: " + book.getTitle());
        }
    }

    public void removeFavorite(Book book) {
        favorites.remove(book);
        NotificationActivity.addNotification("Removed from Favorites: " + book.getTitle());
    }

    public void removeFavoriteAt(int index) {
        if (index >= 0 && index < favorites.size()) {
            Book b = favorites.remove(index);
            NotificationActivity.addNotification("Removed from Favorites: " + b.getTitle());
        }
    }

    public List<Book> getFavorites() { return favorites; }

    /* ---- Cart ---- */
    public void addToCart(Book book) {
        cart.add(book);
        NotificationActivity.addNotification("Added to Cart: " + book.getTitle());
    }

    public void removeFromCart(Book book) {
        cart.remove(book);
        NotificationActivity.addNotification("Removed from Cart: " + book.getTitle());
    }

    public void removeFromCartAt(int index) {
        if (index >= 0 && index < cart.size()) {
            Book b = cart.remove(index);
            NotificationActivity.addNotification("Removed from Cart: " + b.getTitle());
        }
    }

    public List<Book> getCart() { return cart; }

    public double getTotalCartPrice() {
        double total = 0;
        synchronized (cart) {
            for (Book b : cart) total += b.getPrice();
        }
        return total;
    }

    public void clearCart() { cart.clear(); }
}
