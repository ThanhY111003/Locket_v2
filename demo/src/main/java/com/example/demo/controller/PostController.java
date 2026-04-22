package com.example.demo.controller;

import com.example.demo.dto.request.CommentRequest;
import com.example.demo.dto.response.CommentResponse;
import com.example.demo.dto.response.PostResponse;
import com.example.demo.service.CommentService;
import com.example.demo.service.LikeService;
import com.example.demo.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Tạo, tìm kiếm, xoá bài đăng, like và comment")
@SecurityRequirement(name = "bearerAuth")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final LikeService likeService;

    /* ======================== POST CRUD ======================== */

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Đăng bài mới với ảnh và giao dịch chi tiêu")
    public ResponseEntity<PostResponse> createPost(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestPart("image") MultipartFile image,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "caption", required = false) String caption,
            @RequestParam("transactionDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate transactionDate,
            @RequestParam(value = "notes", required = false) String notes
    ) {
        PostResponse response = postService.createPost(
                currentUser.getUsername(), image, amount, categoryId, caption, transactionDate, notes);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "Xoá bài đăng của mình")
    public ResponseEntity<Map<String, String>> deletePost(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable UUID postId
    ) {
        postService.deletePost(postId, currentUser.getUsername());
        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm bài đăng theo từ khoá (caption, ghi chú)")
    public ResponseEntity<Page<PostResponse>> searchPosts(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(postService.searchPosts(keyword, currentUser.getUsername(), page, size));
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy tất cả bài đăng của mình")
    public ResponseEntity<List<PostResponse>> getMyPosts(
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        return ResponseEntity.ok(postService.getMyPosts(currentUser.getUsername()));
    }

    /* ======================== LIKES ======================== */

    @PostMapping("/{postId}/like")
    @Operation(summary = "Toggle like/unlike bài đăng")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable UUID postId
    ) {
        return ResponseEntity.ok(likeService.toggleLike(postId, currentUser.getUsername()));
    }

    /* ======================== COMMENTS ======================== */

    @GetMapping("/{postId}/comments")
    @Operation(summary = "Lấy tất cả comment của bài đăng")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable UUID postId) {
        return ResponseEntity.ok(commentService.getComments(postId));
    }

    @PostMapping("/{postId}/comments")
    @Operation(summary = "Thêm comment vào bài đăng")
    public ResponseEntity<CommentResponse> addComment(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable UUID postId,
            @Valid @RequestBody CommentRequest request
    ) {
        CommentResponse comment = commentService.addComment(postId, currentUser.getUsername(), request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Xoá comment của mình")
    public ResponseEntity<Map<String, String>> deleteComment(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable UUID commentId
    ) {
        commentService.deleteComment(commentId, currentUser.getUsername());
        return ResponseEntity.ok(Map.of("message", "Comment deleted"));
    }
}
