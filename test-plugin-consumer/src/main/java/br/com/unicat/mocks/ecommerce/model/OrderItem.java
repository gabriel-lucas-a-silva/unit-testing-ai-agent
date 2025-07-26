package br.com.unicat.mocks.ecommerce.model;

public class OrderItem {
	private Product product;
	private int quantity;
	private double price;
	private double tax;

	public OrderItem(Product product, int quantity, double price, double tax) {
		this.product = product;
		this.quantity = quantity;
		this.price = price;
		this.tax = tax;
	}

	// Getters
	public Product getProduct() { return product; }
	public int getQuantity() { return quantity; }
	public double getPrice() { return price; }
	public double getTax() { return tax; }
}
