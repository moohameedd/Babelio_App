package com.example.tp;

import java.io.Serializable;
import java.util.Objects;

public class Book implements Serializable {
    private String key;
    private String title;
    private String author;
    private String cover_id;
    private double price;
    private float userRating = 0f;   // This will store the average rating (API + User)
    private int ratingCount = 0;

    public Book(String title, String author, String cover_id) {
        this.title = title;
        this.author = author;
        this.cover_id = cover_id;
        this.price = 10 + (Math.random() * 40);
    }

    public Book(String key, String title, String author, String cover_id) {
        this.key = key;
        this.title = title;
        this.author = author;
        this.cover_id = cover_id;
        this.price = 10 + (Math.random() * 40);
    }

    public Book(String key, String title, String author, String cover_id, double rating) {
        this.key = key;
        this.title = title;
        this.author = author;
        this.cover_id = cover_id;
        this.userRating = (float) rating;
        this.ratingCount = rating > 0 ? 1 : 0;
        this.price = 10 + (Math.random() * 40);
    }

    public String getKey()   { return key; }
    public String getTitle() { return title; }
    public String getAuthor(){ return author; }
    public double getPrice() { return price; }

    public String getCoverUrl() {
        if (cover_id == null || cover_id.isEmpty()) return null;
        return "https://covers.openlibrary.org/b/id/" + cover_id + "-L.jpg";
    }

    public String getCoverId() { return cover_id; }

    /* ---- Rating helpers ---- */
    public float getUserRating()  { return userRating; }
    public int   getRatingCount() { return ratingCount; }

    public void addRating(float stars) {
        // Running average
        float total = userRating * ratingCount + stars;
        ratingCount++;
        userRating = total / ratingCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(key, book.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
