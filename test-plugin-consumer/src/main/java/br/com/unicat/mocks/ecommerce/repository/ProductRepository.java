package br.com.unicat.mocks.ecommerce.repository;

import br.com.unicat.mocks.ecommerce.model.Product;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class ProductRepository {
	private final Map<String, Product> products = new HashMap<>();

	public ProductRepository() {
		products.put("1", new Product("1", "Laptop", 1500.0, 10, 2.5));
		products.put("2", new Product("2", "Smartphone", 800.0, 20, 0.5));
	}

	public Optional<Product> findById(String productId) {
		return Optional.ofNullable(products.get(productId));
	}

	public void save(Product product) {
		products.put(product.getId(), product);
	}
}
