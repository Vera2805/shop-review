package com.example.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderCreateRequest {
    @NotBlank(message = "Description cannot be blank")
    private String description;
}