package com.example.demo.controller;

import com.example.demo.dto.response.FriendshipResponse;
import com.example.demo.dto.request.FriendRequest;
import com.example.demo.entity.Friendship;
import com.example.demo.entity.FriendshipStatus;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.repository.FriendshipRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.FriendshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@Tag(name = "Friendships", description = "Quản lý bạn bè")
@SecurityRequirement(name = "bearerAuth")
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    @PostMapping("/request")
    @Operation(summary = "Gửi lời mời kết bạn")
    public ResponseEntity<Map<String, String>> sendFriendRequest(
            @AuthenticationPrincipal UserDetails currentUser,
            @Valid @RequestBody FriendRequest request
    ) {
        friendshipService.sendFriendRequest(currentUser.getUsername(), request.getUserId());
        return ResponseEntity.ok(Map.of("message", "Friend request sent successfully"));
    }

    @PutMapping("/accept/{senderUserId}")
    @Operation(summary = "Chấp nhận lời mời kết bạn")
    public ResponseEntity<Map<String, String>> acceptFriendRequest(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable UUID senderUserId
    ) {
        friendshipService.acceptFriendRequest(currentUser.getUsername(), senderUserId);
        return ResponseEntity.ok(Map.of("message", "Friend request accepted"));
    }

    @PutMapping("/reject/{senderUserId}")
    @Operation(summary = "Từ chối lời mời kết bạn")
    public ResponseEntity<Map<String, String>> rejectFriendRequest(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable UUID senderUserId
    ) {
        friendshipService.rejectFriendRequest(currentUser.getUsername(), senderUserId);
        return ResponseEntity.ok(Map.of("message", "Friend request rejected"));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách bạn bè đã chấp nhận")
    public ResponseEntity<List<FriendshipResponse>> getFriends(
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.ok(friendshipService.getFriends(currentUser.getUsername()));
    }

    @GetMapping("/pending")
    @Operation(summary = "Lấy danh sách lời mời kết bạn chờ xác nhận")
    public ResponseEntity<List<FriendshipResponse>> getPendingRequests(
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        List<Friendship> pending = friendshipRepository.findPendingRequests(user.getId());
        List<FriendshipResponse> responses = pending.stream()
                .map(f -> {
                    User sender = userRepository.findById(f.getId().getUserId1())
                            .orElse(null);
                    if (sender == null) return null;
                    return FriendshipResponse.builder()
                            .friendId(sender.getId())
                            .username(sender.getUsername())
                            .fullName(sender.getFullName())
                            .profilePictureUrl(sender.getProfilePictureUrl())
                            .createdAt(f.getCreatedAt())
                            .build();
                })
                .filter(r -> r != null)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{friendId}")
    @Operation(summary = "Huỷ kết bạn")
    public ResponseEntity<Map<String, String>> removeFriend(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable UUID friendId
    ) {
        friendshipService.removeFriend(currentUser.getUsername(), friendId);
        return ResponseEntity.ok(Map.of("message", "Friend removed"));
    }
}
