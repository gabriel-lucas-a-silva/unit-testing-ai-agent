package br.com.unicat.mocks.ecommerce.service;

import org.springframework.stereotype.Service;

@Service
public class TaxService {
	public double getTaxRate(String state) {
		// Simula taxas de imposto com base no estado
		switch (state) {
			case "SP":
				return 0.18; // 18% de imposto
			case "RJ":
				return 0.20; // 20% de imposto
			default:
				return 0.10; // 10% de imposto padrão
		}
	}
}
