package cs308.backhend.controller;

import cs308.backhend.dto.OrderSummaryResponse;
import cs308.backhend.model.Address;
import cs308.backhend.model.Card;
import cs308.backhend.model.Comment;
import cs308.backhend.model.Order;
import cs308.backhend.model.User;
import cs308.backhend.repository.AddressRepo;
import cs308.backhend.repository.CardRepo;
import cs308.backhend.repository.CommentRepository;
import cs308.backhend.repository.OrderRepo;
import cs308.backhend.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserController {

    private final UserRepo userRepo;
    private final AddressRepo addressRepository;
    private final CardRepo creditCardRepository;
    private final OrderRepo orderRepo;
    private final CommentRepository commentRepository;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(@RequestParam String email) {
        Optional<User> userOptional = userRepo.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        User user = userOptional.get();

        List<String> addresses = addressRepository.findByUserId(user.getId())
                .stream()
                .map(Address::getAddress)
                .collect(Collectors.toList());

        List<String> cards = creditCardRepository.findByUserId(user.getId())
                .stream()
                .map(Card::getLast4Digits)
                .collect(Collectors.toList());

        UserProfileResponse response = new UserProfileResponse();
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setAddresses(addresses);
        response.setCards(cards);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderSummaryResponse>> getUserOrders(@RequestParam String email) {
        Optional<User> userOptional = userRepo.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        User user = userOptional.get();
        List<Order> orders = orderRepo.findByUser_Id(user.getId());

        List<OrderSummaryResponse> orderResponses = orders.stream().map(order -> {
            OrderSummaryResponse o = new OrderSummaryResponse();
            o.setProductName(order.getProduct().getName());
            o.setCardLast4(order.getCard().getLast4Digits());
            o.setAddressText(order.getAddress().getAddress());
            o.setQuantity(order.getQuantity());
            o.setStatus(order.getStatus().toString());
            o.setCreatedAt(order.getCreatedAt());
            o.setProductImageUrl(order.getProduct().getImageUrl());
            return o;
        }).toList();

        return ResponseEntity.ok(orderResponses);
    }
}