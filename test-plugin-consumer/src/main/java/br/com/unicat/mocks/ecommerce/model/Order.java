package br.com.unicat.mocks.ecommerce.model;

import java.util.List;

public class Order {
	private User user;
	private List<OrderItem> items;
	private ShippingInfo shippingInfo;
	private PaymentInfo paymentInfo;
	private double subtotal;
	private double taxTotal;
	private double totalAmount;

	public Order(User user, List<OrderItem> items, ShippingInfo shippingInfo, PaymentInfo paymentInfo, double subtotal, double taxTotal, double totalAmount) {
		this.user = user;
		this.items = items;
		this.shippingInfo = shippingInfo;
		this.paymentInfo = paymentInfo;
		this.subtotal = subtotal;
		this.taxTotal = taxTotal;
		this.totalAmount = totalAmount;
	}

	// Getters
	public User getUser() { return user; }
	public List<OrderItem> getItems() { return items; }
	public ShippingInfo getShippingInfo() { return shippingInfo; }
	public PaymentInfo getPaymentInfo() { return paymentInfo; }
	public double getSubtotal() { return subtotal; }
	public double getTaxTotal() { return taxTotal; }
	public double getTotalAmount() { return totalAmount; }
}
