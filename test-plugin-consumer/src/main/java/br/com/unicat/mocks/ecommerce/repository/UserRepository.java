package br.com.unicat.mocks.ecommerce.repository;

import br.com.unicat.mocks.ecommerce.model.Address;
import br.com.unicat.mocks.ecommerce.model.CreditCard;
import br.com.unicat.mocks.ecommerce.model.PayPalAccount;
import br.com.unicat.mocks.ecommerce.model.User;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepository {
	private final Map<String, User> users = new HashMap<>();

	public UserRepository() {
		Address address = new Address("SP");
		CreditCard creditCard = new CreditCard(5000.0);
		PayPalAccount payPalAccount = new PayPalAccount(3000.0);
		User user = new User("1", "user@example.com", address);
		user = new User("1", "user@example.com", address);
		user.setCreditCard(creditCard);
		user.setPayPalAccount(payPalAccount);
		users.put("1", user);
	}

	public Optional<User> findById(String userId) {
		return Optional.ofNullable(users.get(userId));
	}
}


