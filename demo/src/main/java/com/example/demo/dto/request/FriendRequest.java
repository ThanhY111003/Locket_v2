package com.example.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class FriendRequest {

    @NotNull(message = "Target user ID is required")
    private UUID userId;
}
