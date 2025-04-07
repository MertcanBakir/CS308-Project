package cs308.backhend.service;

import cs308.backhend.model.Order;
import cs308.backhend.model.OrderStatus;
import cs308.backhend.repository.OrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepo orderRepo;

}