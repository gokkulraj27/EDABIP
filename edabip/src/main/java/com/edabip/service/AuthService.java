package com.edabip.service;

import com.edabip.dto.request.LoginRequest;
import com.edabip.dto.request.RegisterRequest;
import com.edabip.dto.response.AuthResponse;
import com.edabip.entity.Role;
import com.edabip.entity.User;
import com.edabip.entity.enums.RoleName;
import com.edabip.exception.BadRequestException;
import com.edabip.repository.RoleRepository;
import com.edabip.repository.UserRepository;
import com.edabip.security.CustomUserDetails;
import com.edabip.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditService auditService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        String token = tokenProvider.generateToken(auth);
        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();

        Set<String> roles = principal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

        auditService.log(principal.getId(), principal.getUsername(), "LOGIN", "USER", principal.getId(), "User logged in");

        return AuthResponse.builder()
            .token(token)
            .type("Bearer")
            .id(principal.getId())
            .username(principal.getUsername())
            .email(principal.getEmail())
            .roles(roles)
            .build();
    }

    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email '" + request.getEmail() + "' is already in use");
        }

        Set<Role> roles = resolveRoles(request.getRoles());

        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .roles(roles)
            .active(true)
            .build();

        User saved = userRepository.save(user);
        auditService.log(saved.getId(), saved.getUsername(), "REGISTER", "USER", saved.getId(), "New user registered");

        return "User registered successfully";
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        if (roleNames == null || roleNames.isEmpty()) {
            roles.add(getRole(RoleName.ROLE_EMPLOYEE));
        } else {
            for (String name : roleNames) {
                try {
                    roles.add(getRole(RoleName.valueOf(name)));
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("Invalid role: " + name);
                }
            }
        }
        return roles;
    }

    private Role getRole(RoleName name) {
        return roleRepository.findByName(name)
            .orElseThrow(() -> new BadRequestException("Role not found: " + name));
    }
}
