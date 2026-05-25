package com.example.orderservice.dto;

import com.example.orderservice.entity.OrderStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID id;
    private String description;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private UUID userId;
}
