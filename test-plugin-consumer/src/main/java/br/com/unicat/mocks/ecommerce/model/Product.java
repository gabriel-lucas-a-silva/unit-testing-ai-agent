package br.com.unicat.mocks.ecommerce.model;

public class Product {
	private String id;
	private String name;
	private double price;
	private int stock;
	private double weight;

	public Product(String id, String name, double price, int stock, double weight) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.stock = stock;
		this.weight = weight;
	}

	// Getters and Setters
	public String getId() { return id; }
	public String getName() { return name; }
	public double getPrice() { return price; }
	public int getStock() { return stock; }
	public void setStock(int stock) { this.stock = stock; }
	public double getWeight() { return weight; }
}
