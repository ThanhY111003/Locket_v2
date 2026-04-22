package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FriendshipResponse {
    // Dùng cho GET /friends
    private UserSummary friend;
    private String status;
    private Instant createdAt;

    // Dùng thêm cho GET /friends/pending (thông tin người gửi lời mời)
    private UUID friendId;
    private String username;
    private String fullName;
    private String profilePictureUrl;
}
