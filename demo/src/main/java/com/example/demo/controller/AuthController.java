package com.example.demo.controller;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Đăng ký và đăng nhập")
public class AuthController {

    private final AuthService authService;

    @org.springframework.web.bind.annotation.GetMapping("/me")
    public ResponseEntity<java.util.Map<String, Object>> getMe(org.springframework.security.core.Authentication auth) {
        if (auth == null) return ResponseEntity.ok(java.util.Map.of("message", "Anonymous"));
        com.example.demo.entity.User user = authService.getUserByUsername(auth.getName());
        return ResponseEntity.ok(java.util.Map.of(
                "authorities", auth.getAuthorities().stream().map(Object::toString).toList(),
                "dbRole", user.getRole() != null ? user.getRole() : "NULL"
        ));
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký người dùng mới")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập và nhận JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }
}
