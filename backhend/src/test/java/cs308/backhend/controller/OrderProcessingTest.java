package cs308.backhend;

import cs308.backhend.model.*;
import cs308.backhend.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class OrderProcessingTest {

    @Autowired private ProductRepo productRepo;
    @Autowired private UserRepo userRepo;
    @Autowired private OrderRepo orderRepo;
    @Autowired private CardRepo cardRepo;
    @Autowired private AddressRepo addressRepo;

    private Product createValidProduct(String name, String serial, BigDecimal price, int stock) {
        Product p = new Product();
        p.setName(name);
        p.setSerialNumber(serial);
        p.setDescription("Test product description");
        p.setQuantityInStock(stock);
        p.setPrice(price);
        p.setWarrantyStatus(true);
        p.setDistributorInfo("Test Distributor");
        p.setImageUrl("http://example.com/image.jpg");
        p.setApproved(true);
        return p;
    }

    @Test
    @DisplayName("should save order with PROCESSING status")
    void shouldSaveOrderWithProcessingStatus() {
        User user = new User();
        user.setFullName("Test User");
        user.setEmail("user@example.com");
        user.setPassword("password");
        user.setRole(Role.User);
        userRepo.save(user);

        Product product = createValidProduct("Test Product", "SN-001", new BigDecimal("29.99"), 10);
        productRepo.save(product);

        Card card = new Card();
        card.setCardNumber("1234567812345678");
        card.setCvv("123");
        card.setName("Test");
        card.setSurname("User");
        card.setCardExpiryDate("12/30");
        card.setUser(user);
        cardRepo.save(card);

        Address address = new Address();
        address.setAddress("Test Address");
        address.setUser(user);
        addressRepo.save(address);

        Order order = new Order();
        order.setUser(user);
        order.setProduct(product);
        order.setCard(card);
        order.setAddress(address);
        order.setQuantity(2);
        order.setStatus(OrderStatus.PROCESSING);
        order.setCreatedAt(LocalDateTime.now());
        orderRepo.save(order);

        var found = orderRepo.findById(order.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.PROCESSING);
    }

    @Test
    @DisplayName("Product manager should update order status to INTRANSIT")
    void productManagerShouldUpdateOrderStatusToInTransit() {
        User user = new User();
        user.setFullName("Manager");
        user.setEmail("manager@example.com");
        user.setPassword("securepass");
        user.setRole(Role.productManager);
        userRepo.save(user);

        Product product = createValidProduct("Boxed Item", "SN-002", new BigDecimal("59.99"), 5);
        productRepo.save(product);

        Card card = new Card();
        card.setCardNumber("8765432187654321");
        card.setCvv("456");
        card.setName("Manager");
        card.setSurname("Admin");
        card.setCardExpiryDate("11/26");
        card.setUser(user);
        cardRepo.save(card);

        Address address = new Address();
        address.setAddress("Manager Address");
        address.setUser(user);
        addressRepo.save(address);

        Order order = new Order();
        order.setUser(user);
        order.setProduct(product);
        order.setCard(card);
        order.setAddress(address);
        order.setQuantity(1);
        order.setStatus(OrderStatus.PROCESSING);
        order.setCreatedAt(LocalDateTime.now());
        orderRepo.save(order);

        order.setStatus(OrderStatus.INTRANSIT);
        orderRepo.save(order);

        var updatedOrder = orderRepo.findById(order.getId());
        assertThat(updatedOrder).isPresent();
        assertThat(updatedOrder.get().getStatus()).isEqualTo(OrderStatus.INTRANSIT);
    }
}