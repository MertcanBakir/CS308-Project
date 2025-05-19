package cs308.backhend;

import cs308.backhend.model.*;
import cs308.backhend.repository.*;
import cs308.backhend.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class OrderCancelRefundTest {

    @Autowired private ProductRepo productRepo;
    @Autowired private UserRepo userRepo;
    @Autowired private OrderRepo orderRepo;
    @Autowired private CardRepo cardRepo;
    @Autowired private AddressRepo addressRepo;
    @Autowired private OrderService orderService;

    private User createUser(String email) {
        User user = new User();
        user.setFullName("Test User");
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(Role.User);
        return userRepo.save(user);
    }

    private Product createProduct(int stock) {
        Product p = new Product();
        p.setName("Test Product");
        p.setSerialNumber("SN-" + System.nanoTime());
        p.setDescription("Test product");
        p.setQuantityInStock(stock);
        p.setPrice(new BigDecimal("100"));
        p.setWarrantyStatus(true);
        p.setDistributorInfo("Distributor");
        p.setImageUrl("http://example.com");
        p.setApproved(true);
        return productRepo.save(p);
    }

    private Card createCard(User user) {
        Card card = new Card();
        card.setCardNumber("1234567890123456");
        card.setCvv("123");
        card.setName("Test");
        card.setSurname("User");
        card.setCardExpiryDate("12/30");
        card.setUser(user);
        return cardRepo.save(card);
    }

    private Address createAddress(User user) {
        Address addr = new Address();
        addr.setAddress("Test Address");
        addr.setUser(user);
        return addressRepo.save(addr);
    }

    private Order createOrder(User user, Product product, OrderStatus status, LocalDateTime createdAt) {
        Order order = new Order();
        order.setUser(user);
        order.setProduct(product);
        order.setCard(createCard(user));
        order.setAddress(createAddress(user));
        order.setQuantity(2);
        order.setStatus(status);
        order.setCreatedAt(createdAt);
        return orderRepo.save(order);
    }

    @Test
    @DisplayName("Customer can cancel order if it is PROCESSING")
    void canCancelProcessingOrder() {
        User user = createUser("a@a.com");
        Product product = createProduct(5);
        Order order = createOrder(user, product, OrderStatus.PROCESSING, LocalDateTime.now());

        orderService.cancelOrder(order);

        assertThat(orderRepo.findById(order.getId()).get().getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("Customer cannot cancel order if it is DELIVERED")
    void cannotCancelDeliveredOrder() {
        User user = createUser("b@b.com");
        Product product = createProduct(5);
        Order order = createOrder(user, product, OrderStatus.DELIVERED, LocalDateTime.now());

        assertThatThrownBy(() -> orderService.cancelOrder(order))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel delivered order");
    }

    @Test
    @DisplayName("Customer can refund order delivered within 30 days")
    void canRefundDeliveredWithin30Days() {
        User user = createUser("c@c.com");
        Product product = createProduct(5);
        Order order = createOrder(user, product, OrderStatus.DELIVERED, LocalDateTime.now().minusDays(10));

        orderService.refundOrder(order);

        assertThat(orderRepo.findById(order.getId()).get().getStatus()).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    @DisplayName("Customer cannot refund order older than 30 days")
    void cannotRefundAfter30Days() {
        User user = createUser("d@d.com");
        Product product = createProduct(5);
        Order order = createOrder(user, product, OrderStatus.DELIVERED, LocalDateTime.now().minusDays(35));

        assertThatThrownBy(() -> orderService.refundOrder(order))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Order too old for refund");
    }

    @Test
    @DisplayName("Refund should add quantity back to stock")
    void refundIncreasesStock() {
        User user = createUser("e@e.com");
        Product product = createProduct(10);
        Order order = createOrder(user, product, OrderStatus.DELIVERED, LocalDateTime.now().minusDays(5));

        int before = product.getQuantityInStock();
        orderService.refundOrder(order);

        int after = productRepo.findById(product.getId()).get().getQuantityInStock();
        assertThat(after).isEqualTo(before + order.getQuantity());
    }
}
