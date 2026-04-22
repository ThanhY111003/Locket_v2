package com.example.demo.service;

import com.example.demo.repository.UserRepository;
import com.example.demo.repository.PostRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    public void deleteUserSafely(UUID userId) {
        log.info("Admin deleting user: {}", userId);

        // 1. Delete user's comments
        entityManager.createQuery("DELETE FROM Comment c WHERE c.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();

        // 2. Delete user's likes
        entityManager.createQuery("DELETE FROM PostLike pl WHERE pl.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();

        // 3. Delete user's pending/accepted friendships
        entityManager.createQuery("DELETE FROM Friendship f WHERE f.requester.id = :userId OR f.addressee.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();

        // 4. For posts owned by user, need to delete their likes, comments, transactions first
        // Delete all likes for user's posts
        entityManager.createQuery("DELETE FROM PostLike pl WHERE pl.post.id IN (SELECT p.id FROM Post p WHERE p.user.id = :userId)")
                .setParameter("userId", userId)
                .executeUpdate();
        
        // Delete all comments for user's posts
        entityManager.createQuery("DELETE FROM Comment c WHERE c.post.id IN (SELECT p.id FROM Post p WHERE p.user.id = :userId)")
                .setParameter("userId", userId)
                .executeUpdate();

        // Delete all transactions for user's posts
        entityManager.createQuery("DELETE FROM Transaction t WHERE t.post.id IN (SELECT p.id FROM Post p WHERE p.user.id = :userId)")
                .setParameter("userId", userId)
                .executeUpdate();

        // 5. Delete user's posts
        entityManager.createQuery("DELETE FROM Post p WHERE p.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();

        // 6. Delete the user
        userRepository.deleteById(userId);
    }

    @Transactional
    public void deletePostSafely(UUID postId) {
        log.info("Admin deleting post: {}", postId);

        // Delete likes
        entityManager.createQuery("DELETE FROM PostLike pl WHERE pl.post.id = :postId")
                .setParameter("postId", postId)
                .executeUpdate();

        // Delete comments
        entityManager.createQuery("DELETE FROM Comment c WHERE c.post.id = :postId")
                .setParameter("postId", postId)
                .executeUpdate();

        // Delete transaction
        entityManager.createQuery("DELETE FROM Transaction t WHERE t.post.id = :postId")
                .setParameter("postId", postId)
                .executeUpdate();

        // Delete post
        postRepository.deleteById(postId);
    }
}
