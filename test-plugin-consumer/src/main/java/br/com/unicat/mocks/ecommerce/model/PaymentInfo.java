package br.com.unicat.mocks.ecommerce.model;

public class PaymentInfo {
	private boolean paymentSuccessful;
	private String paymentMethod;

	public PaymentInfo(boolean paymentSuccessful, String paymentMethod) {
		this.paymentSuccessful = paymentSuccessful;
		this.paymentMethod = paymentMethod;
	}

	public boolean isPaymentSuccessful() { return paymentSuccessful; }
	public String getPaymentMethod() { return paymentMethod; }
}
