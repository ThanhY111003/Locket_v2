package com.example.demo.controller;

import com.example.demo.dto.response.UserProfileResponse;
import com.example.demo.dto.response.UserSummary;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Quản lý người dùng")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin profile của user hiện tại")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.ok(userService.getMyProfile(currentUser.getUsername()));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm user để kết bạn")
    public ResponseEntity<List<UserSummary>> searchUsers(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestParam("q") String query
    ) {
        return ResponseEntity.ok(userService.searchUsers(query, currentUser.getUsername()));
    }
}
