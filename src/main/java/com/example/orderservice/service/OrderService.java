package com.example.orderservice.service;

import com.example.orderservice.dto.OrderCreateRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.OrderUpdateStatusRequest;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.entity.User;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderResponse createOrder(OrderCreateRequest request) {
        User currentUser = getCurrentUserEntity();
        Order order = Order.builder()
                .user(currentUser)
                .description(request.getDescription())
                .status(OrderStatus.CREATED)
                .build();
        return mapToResponse(orderRepository.save(order));
    }

    public Page<OrderResponse> getMyOrders(Pageable pageable) {
        User currentUser = getCurrentUserEntity();
        return orderRepository.findByUser(currentUser, pageable)
                .map(this::mapToResponse);
    }

    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public OrderResponse updateOrderStatus(UUID orderId, OrderUpdateStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(request.getStatus());
        return mapToResponse(orderRepository.save(order));
    }

    public void deleteOrder(UUID orderId, UUID currentUserId, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        // Проверка: владелец или админ
        if (!isAdmin && !order.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("You can delete only your own orders");
        }
        orderRepository.delete(order);
    }

    private User getCurrentUserEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .description(order.getDescription())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .userId(order.getUser().getId())
                .build();
    }

    public UUID getCurrentUserId() {
        return getCurrentUserEntity().getId();
    }
}
