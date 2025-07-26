package br.com.unicat.mocks.ecommerce.model;

public class CartItem {
	private String productId;
	private int quantity;
	private double price;
	private double tax;

	public CartItem(String productId, int quantity, double price) {
		this.productId = productId;
		this.quantity = quantity;
		this.price = price;
	}

	// Getters and Setters
	public String getProductId() { return productId; }
	public int getQuantity() { return quantity; }
	public double getPrice() { return price; }
	public void setPrice(double price) { this.price = price; }
	public double getTax() { return tax; }
	public void setTax(double tax) { this.tax = tax; }
}
