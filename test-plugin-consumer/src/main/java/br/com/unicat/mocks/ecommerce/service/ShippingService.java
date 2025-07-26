package br.com.unicat.mocks.ecommerce.service;

import br.com.unicat.mocks.ecommerce.model.ShippingInfo;
import org.springframework.stereotype.Service;

@Service
public class ShippingService {
	public ShippingInfo calculateShippingCost(String shippingAddress, double totalWeight) {
		// Simula cálculo de custo de envio
		double cost = totalWeight * 5.0; // Custo por peso
		if (shippingAddress.contains("Remota")) {
			cost *= 1.5; // Aumenta o custo para áreas remotas
		}
		return new ShippingInfo(cost);
	}
}
