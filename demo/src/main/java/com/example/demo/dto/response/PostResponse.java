package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PostResponse {
    private UUID id;
    private String imageUrl;
    private String caption;
    private Instant createdAt;
    private UserSummary user;
    private TransactionInfo transaction;
    private String suggestedCategory;

    // Social features
    private long likeCount;
    private boolean likedByMe;
    private long commentCount;
}
