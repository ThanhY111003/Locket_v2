package com.example.demo.service;

import com.example.demo.entity.Post;
import com.example.demo.entity.PostLike;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.repository.PostLikeRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * Toggle like: nếu đã like → unlike, chưa like → like
     * @return Map chứa liked (boolean) và likeCount (long)
     */
    @Transactional
    public Map<String, Object> toggleLike(UUID postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException("Post not found", HttpStatus.NOT_FOUND));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        Optional<PostLike> existing = postLikeRepository.findByPostIdAndUserId(postId, user.getId());

        boolean liked;
        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
            liked = false;
        } else {
            postLikeRepository.save(PostLike.builder().post(post).user(user).build());
            liked = true;
        }

        long count = postLikeRepository.countByPostId(postId);
        return Map.of("liked", liked, "likeCount", count);
    }
}
