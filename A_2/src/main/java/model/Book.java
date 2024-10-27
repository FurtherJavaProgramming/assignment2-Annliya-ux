package model;

public class Book {
    private int id; 
    private String title;
    private String authors;
    private int physicalCopies;
    private double price;
    private int soldCopies;

    public Book(int bookId, String title, String authors, int physicalCopies, double price, int soldCopies) {
        this.id = bookId;
        this.title = title;
        this.authors = authors;
        this.physicalCopies = physicalCopies;
        this.price = price;
        this.soldCopies = soldCopies;
    }

    public Book(String title, String authors, int physicalCopies, double price, int soldCopies) {
        this.title = title;
        this.authors = authors;
        this.physicalCopies = physicalCopies;
        this.price = price;
        this.soldCopies = soldCopies;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthors() { return authors; }
    public void setAuthors(String authors) { this.authors = authors; }
    public int getPhysicalCopies() { return physicalCopies; }
    public void setPhysicalCopies(int physicalCopies) { this.physicalCopies = physicalCopies; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getSoldCopies() { return soldCopies; }
    public void setSoldCopies(int soldCopies) { this.soldCopies = soldCopies; }
}
