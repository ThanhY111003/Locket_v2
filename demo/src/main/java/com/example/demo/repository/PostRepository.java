package com.example.demo.repository;

import com.example.demo.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    /**
     * Lấy bài đăng từ danh sách user IDs (bạn bè), sắp xếp theo thời gian mới nhất.
     * Dùng cho Feed.
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.user.id IN :userIds ORDER BY p.createdAt DESC")
    Page<Post> findFeedByUserIds(@Param("userIds") List<UUID> userIds, Pageable pageable);

    List<Post> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("""
            SELECT p FROM Post p JOIN FETCH p.user
            WHERE LOWER(p.caption) LIKE LOWER(CONCAT('%', :q, '%'))
               OR EXISTS (SELECT t FROM Transaction t WHERE t.post = p AND LOWER(t.notes) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY p.createdAt DESC
            """)
    Page<Post> searchByKeyword(@Param("q") String q, Pageable pageable);
}
