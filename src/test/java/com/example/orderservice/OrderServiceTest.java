package com.example.orderservice;

import com.example.orderservice.dto.OrderCreateRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OrderStatus;
import com.example.orderservice.entity.Role;
import com.example.orderservice.entity.User;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.repository.UserRepository;
import com.example.orderservice.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .password("encoded")
                .role(Role.USER)
                .build();

        testOrder = Order.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .description("Test order")
                .status(OrderStatus.CREATED)
                .build();

        // Устанавливаем SecurityContext, но без стабов (они будут в конкретных тестах)
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext(); // очистка после каждого теста
    }

    @Test
    void createOrder_Success() {
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setDescription("Test order");

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals("Test order", response.getDescription());
        assertEquals(OrderStatus.CREATED, response.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void deleteOrder_AsOwner_Success() {
        when(orderRepository.findById(testOrder.getId())).thenReturn(Optional.of(testOrder));
        orderService.deleteOrder(testOrder.getId(), testUser.getId(), false);
        verify(orderRepository, times(1)).delete(testOrder);
    }

    @Test
    void deleteOrder_AsAdmin_AnyOrder() {
        when(orderRepository.findById(testOrder.getId())).thenReturn(Optional.of(testOrder));
        orderService.deleteOrder(testOrder.getId(), UUID.randomUUID(), true);
        verify(orderRepository, times(1)).delete(testOrder);
    }

    @Test
    void deleteOrder_NotOwner_ThrowsException() {
        when(orderRepository.findById(testOrder.getId())).thenReturn(Optional.of(testOrder));
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                orderService.deleteOrder(testOrder.getId(), UUID.randomUUID(), false));
        assertEquals("You can delete only your own orders", exception.getMessage());
        verify(orderRepository, never()).delete(any());
    }
}