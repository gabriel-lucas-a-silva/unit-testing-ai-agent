package br.com.unicat.mocks.ecommerce.service;

import br.com.unicat.mocks.ecommerce.model.CartItem;
import br.com.unicat.mocks.ecommerce.model.Order;
import br.com.unicat.mocks.ecommerce.model.OrderItem;
import br.com.unicat.mocks.ecommerce.model.PaymentInfo;
import br.com.unicat.mocks.ecommerce.model.Product;
import br.com.unicat.mocks.ecommerce.model.ShippingInfo;
import br.com.unicat.mocks.ecommerce.model.User;
import br.com.unicat.mocks.ecommerce.repository.ProductRepository;
import br.com.unicat.mocks.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ShoppingCartService {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DiscountService discountService;

	@Autowired
	private TaxService taxService;

	@Autowired
	private ShippingService shippingService;

	public Order checkout(String userId, List<CartItem> cartItems, String shippingAddress, String paymentMethod) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
		validateCartItems(cartItems);
		applyProductDiscounts(cartItems, user);
		calculateTaxes(cartItems, user);
		ShippingInfo shippingInfo = calculateShipping(cartItems, shippingAddress);
		PaymentInfo paymentInfo = processPayment(cartItems, user, paymentMethod);

		if (!paymentInfo.isPaymentSuccessful()) {
			throw new RuntimeException("Payment failed");
		}

		Order order = createOrder(user, cartItems, shippingInfo, paymentInfo);
		sendConfirmationEmail(user, order);
		return order;
	}

	private void validateCartItems(List<CartItem> cartItems) {
		for (CartItem item : cartItems) {
			Product product = productRepository.findById(item.getProductId()).orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));
			if (product.getStock() < item.getQuantity()) {
				throw new RuntimeException("Insufficient stock for product: " + product.getName());
			}
		}
	}

	private void applyProductDiscounts(List<CartItem> cartItems, User user) {
		Map<String, Double> discountMap = discountService.getApplicableDiscounts(user.getId());
		for (CartItem item : cartItems) {
			Product product = productRepository.findById(item.getProductId()).orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));
			double discount = discountMap.getOrDefault(product.getId(), 0.0);
			double discountedPrice = product.getPrice() * (1 - discount);
			item.setPrice(discountedPrice);
		}
	}

	private void calculateTaxes(List<CartItem> cartItems, User user) {
		double taxRate = taxService.getTaxRate(user.getAddress().getState());
		for (CartItem item : cartItems) {
			double taxAmount = item.getPrice() * taxRate;
			item.setTax(taxAmount);
		}
	}

	private ShippingInfo calculateShipping(List<CartItem> cartItems, String shippingAddress) {
		double totalWeight = cartItems.stream().mapToDouble(item -> {
			Product product = productRepository.findById(item.getProductId()).orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));
			return product.getWeight() * item.getQuantity();
		}).sum();

		return shippingService.calculateShippingCost(shippingAddress, totalWeight);
	}

	private PaymentInfo processPayment(List<CartItem> cartItems, User user, String paymentMethod) {
		double totalAmount = cartItems.stream().mapToDouble(item -> item.getPrice() * item.getQuantity() + item.getTax()).sum();
		return processPaymentWithMethod(user, totalAmount, paymentMethod);
	}

	private PaymentInfo processPaymentWithMethod(User user, double totalAmount, String paymentMethod) {
		if ("credit_card".equals(paymentMethod)) {
			return processCreditCardPayment(user, totalAmount);
		} else if ("paypal".equals(paymentMethod)) {
			return processPayPalPayment(user, totalAmount);
		} else {
			throw new RuntimeException("Unsupported payment method");
		}
	}

	private PaymentInfo processCreditCardPayment(User user, double totalAmount) {
		// Simulate credit card processing logic
		boolean isSuccessful = user.getCreditCard() != null && user.getCreditCard().getBalance() >= totalAmount;
		return new PaymentInfo(isSuccessful, "Credit Card");
	}

	private PaymentInfo processPayPalPayment(User user, double totalAmount) {
		// Simulate PayPal processing logic
		boolean isSuccessful = user.getPayPalAccount() != null && user.getPayPalAccount().getBalance() >= totalAmount;
		return new PaymentInfo(isSuccessful, "PayPal");
	}

	private Order createOrder(User user, List<CartItem> cartItems, ShippingInfo shippingInfo, PaymentInfo paymentInfo) {
		double subtotal = cartItems.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
		double taxTotal = cartItems.stream().mapToDouble(CartItem::getTax).sum();
		double totalAmount = subtotal + taxTotal + shippingInfo.getCost();

		List<OrderItem> orderItems = new ArrayList<>();
		for (CartItem item : cartItems) {
			Product product = productRepository.findById(item.getProductId()).orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));
			product.setStock(product.getStock() - item.getQuantity());
			productRepository.save(product);
			orderItems.add(new OrderItem(product, item.getQuantity(), item.getPrice(), item.getTax()));
		}

		return new Order(user, orderItems, shippingInfo, paymentInfo, subtotal, taxTotal, totalAmount);
	}

	private void sendConfirmationEmail(User user, Order order) {
		// Simulate sending confirmation email
		System.out.println("Sending confirmation email to: " + user.getEmail());
	}
}
