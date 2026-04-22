package com.example.demo.controller;

import com.example.demo.dto.response.PostResponse;
import com.example.demo.service.FeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
@Tag(name = "Feed", description = "Xem bài đăng từ bạn bè")
@SecurityRequirement(name = "bearerAuth")
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    @Operation(summary = "Lấy danh sách bài đăng từ bạn bè (có phân trang)")
    public ResponseEntity<Page<PostResponse>> getFeed(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<PostResponse> feed = feedService.getFeed(currentUser.getUsername(), page, size);
        return ResponseEntity.ok(feed);
    }
}
