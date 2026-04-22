package com.example.demo.repository;

import com.example.demo.entity.Friendship;
import com.example.demo.entity.FriendshipId;
import com.example.demo.entity.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {

    /**
     * Tìm tất cả bạn bè đã ACCEPTED của một user (theo cả hai chiều).
     */
    @Query("""
            SELECT f FROM Friendship f
            WHERE (f.id.userId1 = :userId OR f.id.userId2 = :userId)
              AND f.status = :status
            """)
    List<Friendship> findByUserIdAndStatus(
            @Param("userId") UUID userId,
            @Param("status") FriendshipStatus status
    );

    /**
     * Tìm friendship request giữa hai user (theo một chiều cụ thể).
     */
    Optional<Friendship> findById(FriendshipId id);

    /**
     * Kiểm tra xem đã có friendship nào giữa hai user chưa (theo cả hai chiều).
     */
    @Query("""
            SELECT COUNT(f) > 0 FROM Friendship f
            WHERE (f.id.userId1 = :u1 AND f.id.userId2 = :u2)
               OR (f.id.userId1 = :u2 AND f.id.userId2 = :u1)
            """)
    boolean existsBetweenUsers(@Param("u1") UUID u1, @Param("u2") UUID u2);

    /**
     * Lấy friendship request từ sender đến receiver (theo một chiều).
     */
    @Query("""
            SELECT f FROM Friendship f
            WHERE (f.id.userId1 = :senderId AND f.id.userId2 = :receiverId)
               OR (f.id.userId1 = :receiverId AND f.id.userId2 = :senderId)
            """)
    Optional<Friendship> findBetweenUsers(@Param("senderId") UUID senderId, @Param("receiverId") UUID receiverId);

    /** Đếm số bạn bè ACCEPTED */
    @Query("""
            SELECT COUNT(f) FROM Friendship f
            WHERE (f.id.userId1 = :userId OR f.id.userId2 = :userId)
              AND f.status = com.example.demo.entity.FriendshipStatus.ACCEPTED
            """)
    long countAcceptedFriends(@Param("userId") UUID userId);

    /** Lấy danh sách lời mời kết bạn đang chờ (gửi đến user hiện tại) */
    @Query("""
            SELECT f FROM Friendship f
            WHERE f.id.userId2 = :userId
              AND f.status = com.example.demo.entity.FriendshipStatus.PENDING
            """)
    List<Friendship> findPendingRequests(@Param("userId") UUID userId);
}

