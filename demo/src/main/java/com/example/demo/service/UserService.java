package com.example.demo.service;

import com.example.demo.dto.response.UserProfileResponse;
import com.example.demo.dto.response.UserSummary;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.repository.FriendshipRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final FriendshipRepository friendshipRepository;

    /** Lấy profile đầy đủ của user hiện tại */
    public UserProfileResponse getMyProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        long postCount = postRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).size();
        long friendCount = friendshipRepository.countAcceptedFriends(user.getId());

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePictureUrl())
                .postCount(postCount)
                .friendCount(friendCount)
                .build();
    }

    /** Tìm user theo username hoặc fullName (gợi ý kết bạn) */
    public List<UserSummary> searchUsers(String query, String currentUsername) {
        return userRepository.searchByKeyword(query).stream()
                .filter(u -> !u.getUsername().equals(currentUsername))
                .limit(10)
                .map(u -> UserSummary.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .fullName(u.getFullName())
                        .profilePictureUrl(u.getProfilePictureUrl())
                        .build())
                .toList();
    }
}
