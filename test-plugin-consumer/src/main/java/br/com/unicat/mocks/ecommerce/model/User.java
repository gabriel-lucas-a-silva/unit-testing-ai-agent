package br.com.unicat.mocks.ecommerce.model;

public class User {
	private String id;
	private String email;
	private Address address;
	private CreditCard creditCard;
	private PayPalAccount payPalAccount;

	public User(String id, String email, Address address) {
		this.id = id;
		this.email = email;
		this.address = address;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public CreditCard getCreditCard() {
		return creditCard;
	}

	public void setCreditCard(CreditCard creditCard) {
		this.creditCard = creditCard;
	}

	public PayPalAccount getPayPalAccount() {
		return payPalAccount;
	}

	public void setPayPalAccount(PayPalAccount payPalAccount) {
		this.payPalAccount = payPalAccount;
	}
}
