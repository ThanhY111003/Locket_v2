package com.example.demo.service;

import com.example.demo.dto.response.CommentResponse;
import com.example.demo.dto.response.UserSummary;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /** Lấy tất cả comment của một bài đăng */
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(UUID postId) {
        // Validate post exists
        if (!postRepository.existsById(postId)) {
            throw new AppException("Post not found", HttpStatus.NOT_FOUND);
        }
        return commentRepository.findByPostIdWithUser(postId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** Thêm comment vào bài đăng */
    @Transactional
    public CommentResponse addComment(UUID postId, String username, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException("Post not found", HttpStatus.NOT_FOUND));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(content.trim())
                .build();

        return toResponse(commentRepository.save(comment));
    }

    /** Xoá comment (chỉ chủ sở hữu mới được xoá) */
    @Transactional
    public void deleteComment(UUID commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException("Comment not found", HttpStatus.NOT_FOUND));

        if (!comment.getUser().getUsername().equals(username)) {
            throw new AppException("You can only delete your own comments", HttpStatus.FORBIDDEN);
        }
        commentRepository.delete(comment);
    }

    private CommentResponse toResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .user(UserSummary.builder()
                        .id(comment.getUser().getId())
                        .username(comment.getUser().getUsername())
                        .fullName(comment.getUser().getFullName())
                        .profilePictureUrl(comment.getUser().getProfilePictureUrl())
                        .build())
                .build();
    }
}
