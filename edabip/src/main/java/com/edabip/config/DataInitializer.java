package com.edabip.config;

import com.edabip.entity.Role;
import com.edabip.entity.User;
import com.edabip.entity.enums.RoleName;
import com.edabip.repository.RoleRepository;
import com.edabip.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

 //Seeds the database on startup: ensures all roles exist and creates a default admin account (admin / admin123) if one is not already present. Using the application's own PasswordEncoder guarantees the seeded password matches the documented credentials
 
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        // 1. Ensure every role exists
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                roleRepository.save(new Role(null, roleName));
                log.info("Initialized role: {}", roleName);
            }
        }

        // 2. Ensure a default admin user exists
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found"));

            User admin = User.builder()
                .username("admin")
                .email("admin@edabip.com")
                .password(passwordEncoder.encode("admin123"))
                .active(true)
                .roles(Set.of(adminRole))
                .build();

            userRepository.save(admin);
            log.info("Created default admin user (username: admin / password: admin123)");
        }

        log.info("Data initialization complete.");
    }
}
