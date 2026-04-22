package com.example.demo.service;

import com.example.demo.dto.response.PostResponse;
import com.example.demo.entity.Friendship;
import com.example.demo.entity.FriendshipStatus;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.repository.FriendshipRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final TransactionRepository transactionRepository;
    private final PostService postService;

    @Transactional(readOnly = true)
    public Page<PostResponse> getFeed(String username, int page, int size) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        List<UUID> friendIds = friendshipRepository
                .findByUserIdAndStatus(currentUser.getId(), FriendshipStatus.ACCEPTED)
                .stream()
                .map(f -> getFriendId(f, currentUser.getId()))
                .collect(Collectors.toList());

        // Thêm cả bài của bản thân
        friendIds.add(currentUser.getId());

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postRepository.findFeedByUserIds(friendIds, pageable);

        List<PostResponse> responses = posts.getContent().stream()
                .map(post -> {
                    var tx = transactionRepository.findByPostId(post.getId()).orElse(null);
                    return postService.mapToPostResponse(post, tx, null, username);
                })
                .toList();

        return new PageImpl<>(responses, pageable, posts.getTotalElements());
    }

    private UUID getFriendId(Friendship friendship, UUID currentUserId) {
        UUID userId1 = friendship.getId().getUserId1();
        return userId1.equals(currentUserId)
                ? friendship.getId().getUserId2()
                : userId1;
    }
}
