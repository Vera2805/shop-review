package com.example.orderservice.dto;

import com.example.orderservice.entity.Role;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class UserInfoResponse {
    private UUID id;
    private String username;
    private Role role;
}