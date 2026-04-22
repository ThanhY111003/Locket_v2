package com.example.demo.init;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            admin = User.builder()
                    .username("admin")
                    .passwordHash(passwordEncoder.encode("123456"))
                    .email("admin@locketfinance.local")
                    .fullName("System Admin")
                    .role("ROLE_ADMIN")
                    .build();
            userRepository.save(admin);
            log.info("Admin account seeded: admin / 123456 (ROLE_ADMIN)");
        } else {
            // Ensure existing admin has the correct role
            if (!"ROLE_ADMIN".equals(admin.getRole())) {
                admin.setRole("ROLE_ADMIN");
                userRepository.save(admin);
                log.info("Existing admin account updated to ROLE_ADMIN");
            }
        }
    }
}
