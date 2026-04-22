package com.example.demo.service;

import com.example.demo.dto.response.FriendshipResponse;
import com.example.demo.dto.response.UserSummary;
import com.example.demo.entity.Friendship;
import com.example.demo.entity.FriendshipId;
import com.example.demo.entity.FriendshipStatus;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.repository.FriendshipRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    /**
     * Gửi lời mời kết bạn.
     *
     * @param senderUsername Username của người gửi (từ JWT)
     * @param targetUserId   ID của người nhận
     */
    @Transactional
    public void sendFriendRequest(String senderUsername, UUID targetUserId) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new AppException("Sender not found", HttpStatus.NOT_FOUND));

        if (sender.getId().equals(targetUserId)) {
            throw new AppException("Cannot send friend request to yourself", HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra target user tồn tại
        userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException("Target user not found", HttpStatus.NOT_FOUND));

        // Kiểm tra đã có friendship giữa hai user chưa
        if (friendshipRepository.existsBetweenUsers(sender.getId(), targetUserId)) {
            throw new AppException("Friendship already exists or pending", HttpStatus.CONFLICT);
        }

        Friendship friendship = Friendship.builder()
                .id(new FriendshipId(sender.getId(), targetUserId))
                .status(FriendshipStatus.PENDING)
                .build();

        friendshipRepository.save(friendship);
        log.info("Friend request sent: {} → {}", sender.getUsername(), targetUserId);
    }

    /**
     * Chấp nhận lời mời kết bạn.
     *
     * @param receiverUsername Username của người nhận (từ JWT) — để xác nhận quyền
     * @param senderUserId     ID của người đã gửi request
     */
    @Transactional
    public void acceptFriendRequest(String receiverUsername, UUID senderUserId) {
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        Friendship friendship = friendshipRepository
                .findBetweenUsers(senderUserId, receiver.getId())
                .orElseThrow(() -> new AppException("Friend request not found", HttpStatus.NOT_FOUND));

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new AppException("Friend request is not pending", HttpStatus.BAD_REQUEST);
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);
        log.info("Friend request accepted: {} accepted request from {}", receiver.getUsername(), senderUserId);
    }

    /**
     * Lấy danh sách bạn bè đã ACCEPTED.
     *
     * @param username Username từ JWT
     * @return Danh sách FriendshipResponse
     */
    public List<FriendshipResponse> getFriends(String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        List<Friendship> friendships = friendshipRepository
                .findByUserIdAndStatus(currentUser.getId(), FriendshipStatus.ACCEPTED);

        return friendships.stream()
                .map(f -> {
                    UUID friendId = getFriendId(f, currentUser.getId());
                    User friend = userRepository.findById(friendId)
                            .orElseThrow(() -> new AppException("Friend not found", HttpStatus.NOT_FOUND));

                    return FriendshipResponse.builder()
                            .friend(UserSummary.builder()
                                    .id(friend.getId())
                                    .username(friend.getUsername())
                                    .fullName(friend.getFullName())
                                    .profilePictureUrl(friend.getProfilePictureUrl())
                                    .build())
                            .status(f.getStatus().name())
                            .createdAt(f.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private UUID getFriendId(Friendship friendship, UUID currentUserId) {
        UUID userId1 = friendship.getId().getUserId1();
        return userId1.equals(currentUserId)
                ? friendship.getId().getUserId2()
                : userId1;
    }

    /** Từ chối lời mời kết bạn */
    @Transactional
    public void rejectFriendRequest(String receiverUsername, UUID senderUserId) {
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
        Friendship friendship = friendshipRepository
                .findBetweenUsers(senderUserId, receiver.getId())
                .orElseThrow(() -> new AppException("Friend request not found", HttpStatus.NOT_FOUND));
        friendshipRepository.delete(friendship);
        log.info("Friend request rejected by {}", receiver.getUsername());
    }

    /** Huỷ kết bạn */
    @Transactional
    public void removeFriend(String username, UUID friendId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
        Friendship friendship = friendshipRepository
                .findBetweenUsers(user.getId(), friendId)
                .orElseThrow(() -> new AppException("Friendship not found", HttpStatus.NOT_FOUND));
        friendshipRepository.delete(friendship);
        log.info("Friendship removed between {} and {}", username, friendId);
    }
}
