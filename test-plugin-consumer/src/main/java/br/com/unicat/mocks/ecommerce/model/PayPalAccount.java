package br.com.unicat.mocks.ecommerce.model;

public class PayPalAccount {
	private double balance;

	public PayPalAccount(double balance) {
		this.balance = balance;
	}

	public double getBalance() { return balance; }
}
