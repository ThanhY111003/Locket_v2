package com.example.demo.service;

import com.example.demo.dto.response.PostResponse;
import com.example.demo.dto.response.TransactionInfo;
import com.example.demo.dto.response.UserSummary;
import com.example.demo.entity.Category;
import com.example.demo.entity.Post;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final CloudinaryService cloudinaryService;
    private final GeminiService geminiService;

    /** Tạo bài đăng mới */
    @Transactional
    public PostResponse createPost(String username, MultipartFile image, BigDecimal amount,
                                   Integer categoryId, String caption, LocalDate transactionDate, String notes) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        log.info("Uploading image for user: {}", username);
        String imageUrl = cloudinaryService.uploadImage(image);

        String suggestedCategoryName = null;
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new AppException("Category not found", HttpStatus.BAD_REQUEST));
        } else {
            suggestedCategoryName = geminiService.categorizeExpense(imageUrl);
            category = (suggestedCategoryName != null)
                    ? categoryRepository.findByName(suggestedCategoryName)
                            .orElseGet(() -> categoryRepository.findByName("Khác").orElse(null))
                    : categoryRepository.findByName("Khác").orElse(null);
        }

        Post post = postRepository.save(Post.builder()
                .user(user).imageUrl(imageUrl).caption(caption).build());

        Transaction transaction = transactionRepository.save(Transaction.builder()
                .post(post).user(user).amount(amount).category(category)
                .transactionDate(transactionDate).notes(notes).build());

        log.info("Post created: postId={}", post.getId());
        return mapToPostResponse(post, transaction, suggestedCategoryName, username);
    }

    /** Xoá bài đăng (chỉ owner) */
    @Transactional
    public void deletePost(UUID postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException("Post not found", HttpStatus.NOT_FOUND));
        if (!post.getUser().getUsername().equals(username)) {
            throw new AppException("You can only delete your own posts", HttpStatus.FORBIDDEN);
        }
        // Xoá ảnh trên Cloudinary/local
        cloudinaryService.deleteImage(post.getImageUrl());
        postRepository.delete(post);
    }

    /** Tìm bài đăng theo keyword (tìm trong caption và notes) */
    @Transactional(readOnly = true)
    public Page<PostResponse> searchPosts(String keyword, String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.searchByKeyword(keyword, pageable);
        List<PostResponse> responses = posts.getContent().stream()
                .map(p -> {
                    Transaction tx = transactionRepository.findByPostId(p.getId()).orElse(null);
                    return mapToPostResponse(p, tx, null, username);
                })
                .toList();
        return new PageImpl<>(responses, pageable, posts.getTotalElements());
    }

    /** Lấy bài đăng của chính mình */
    @Transactional(readOnly = true)
    public List<PostResponse> getMyPosts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
        return postRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(p -> {
                    Transaction tx = transactionRepository.findByPostId(p.getId()).orElse(null);
                    return mapToPostResponse(p, tx, null, username);
                })
                .toList();
    }

    // ===== Helper =====

    public PostResponse mapToPostResponse(Post post, Transaction transaction,
                                           String suggestedCategory, String viewerUsername) {
        // Lấy like count và trạng thái liked của người xem
        long likeCount = postLikeRepository.countByPostId(post.getId());
        boolean likedByMe = false;
        if (viewerUsername != null) {
            userRepository.findByUsername(viewerUsername).ifPresent(viewer ->
                    likedByMe(post.getId(), viewer.getId()));
            likedByMe = userRepository.findByUsername(viewerUsername)
                    .map(viewer -> postLikeRepository.existsByPostIdAndUserId(post.getId(), viewer.getId()))
                    .orElse(false);
        }
        long commentCount = commentRepository.countByPostId(post.getId());

        UserSummary userSummary = UserSummary.builder()
                .id(post.getUser().getId())
                .username(post.getUser().getUsername())
                .fullName(post.getUser().getFullName())
                .profilePictureUrl(post.getUser().getProfilePictureUrl())
                .build();

        TransactionInfo txInfo = null;
        if (transaction != null) {
            txInfo = TransactionInfo.builder()
                    .id(transaction.getId())
                    .amount(transaction.getAmount())
                    .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                    .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                    .transactionDate(transaction.getTransactionDate())
                    .notes(transaction.getNotes())
                    .createdAt(transaction.getCreatedAt())
                    .build();
        }

        return PostResponse.builder()
                .id(post.getId())
                .imageUrl(post.getImageUrl())
                .caption(post.getCaption())
                .createdAt(post.getCreatedAt())
                .user(userSummary)
                .transaction(txInfo)
                .suggestedCategory(suggestedCategory)
                .likeCount(likeCount)
                .likedByMe(likedByMe)
                .commentCount(commentCount)
                .build();
    }

    private boolean likedByMe(UUID postId, UUID userId) {
        return postLikeRepository.existsByPostIdAndUserId(postId, userId);
    }

    // Legacy static method for FeedService (backward compat)
    public static PostResponse mapToPostResponse(Post post, Transaction transaction, String suggestedCategory) {
        return PostResponse.builder()
                .id(post.getId())
                .imageUrl(post.getImageUrl())
                .caption(post.getCaption())
                .createdAt(post.getCreatedAt())
                .user(UserSummary.builder()
                        .id(post.getUser().getId())
                        .username(post.getUser().getUsername())
                        .fullName(post.getUser().getFullName())
                        .profilePictureUrl(post.getUser().getProfilePictureUrl())
                        .build())
                .transaction(transaction != null ? TransactionInfo.builder()
                        .id(transaction.getId())
                        .amount(transaction.getAmount())
                        .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                        .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                        .transactionDate(transaction.getTransactionDate())
                        .notes(transaction.getNotes())
                        .createdAt(transaction.getCreatedAt())
                        .build() : null)
                .suggestedCategory(suggestedCategory)
                .build();
    }
}
