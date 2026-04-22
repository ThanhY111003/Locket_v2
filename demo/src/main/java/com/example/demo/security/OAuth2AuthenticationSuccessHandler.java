package com.example.demo.security;

import com.example.demo.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    
    // URL của frontend
    private final String frontendUrl = "http://localhost:5173/oauth2/callback";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect.");
            return;
        }

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        // Generate JWT token for the google user based on their username in our DB
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails);

        // Build the return URL with tokens
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .queryParam("token", token)
                .queryParam("username", user.getUsername())
                .queryParam("fullName", URLEncoder.encode(user.getFullName() != null ? user.getFullName() : "", StandardCharsets.UTF_8))
                .queryParam("avatar", URLEncoder.encode(user.getProfilePictureUrl() != null ? user.getProfilePictureUrl() : "", StandardCharsets.UTF_8))
                .queryParam("role", user.getRole() != null ? user.getRole() : "ROLE_USER")
                .build().toUriString();

        log.info("OAuth2 login successful for user: {}", user.getUsername());
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
