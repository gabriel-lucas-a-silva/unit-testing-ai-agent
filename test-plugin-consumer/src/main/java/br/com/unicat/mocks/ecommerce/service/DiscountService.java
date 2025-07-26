package br.com.unicat.mocks.ecommerce.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DiscountService {
	public Map<String, Double> getApplicableDiscounts(String userId) {
		// Simula descontos aplicáveis
		Map<String, Double> discounts = new HashMap<>();
		discounts.put("1", 0.1); // 10% de desconto no produto com ID 1
		discounts.put("2", 0.05); // 5% de desconto no produto com ID 2
		return discounts;
	}
}
