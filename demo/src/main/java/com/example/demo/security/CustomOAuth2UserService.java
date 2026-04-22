package com.example.demo.security;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user", ex);
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String googleId = oauth2User.getAttribute("sub");
        String picture = oauth2User.getAttribute("picture");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Update existing user with Google details if it's not already linked
            if (!"GOOGLE".equals(user.getAuthProvider())) {
                user.setAuthProvider("GOOGLE");
                user.setGoogleId(googleId);
            }
            // Optional: update avatar if they don't have one
            if (user.getProfilePictureUrl() == null && picture != null) {
                user.setProfilePictureUrl(picture);
            }
            userRepository.save(user);
        } else {
            // Create a new user for Google login
            // Generate a random unique username since we require it, or use part of email
            String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
            String finalUsername = baseUsername;
            int counter = 1;
            while (userRepository.existsByUsername(finalUsername)) {
                finalUsername = baseUsername + counter;
                counter++;
            }

            user = User.builder()
                    .username(finalUsername)
                    .passwordHash(java.util.UUID.randomUUID().toString()) // Hack to satisfy DB NOT NULL constraint
                    .email(email)
                    .fullName(name)
                    .profilePictureUrl(picture)
                    .authProvider("GOOGLE")
                    .googleId(googleId)
                    .role("ROLE_USER")
                    .build();
            userRepository.save(user);
            log.info("Created new user from Google Login: {}", finalUsername);
        }

        return new CustomOAuth2User(oauth2User, user);
    }
}
