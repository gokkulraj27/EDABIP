package com.edabip.service;

import com.edabip.dto.response.UserResponse;
import com.edabip.entity.User;
import com.edabip.exception.ResourceNotFoundException;
import com.edabip.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public UserResponse setActiveStatus(Long id, boolean active) {
        User user = findById(id);
        user.setActive(active);
        userRepository.save(user);
        String action = active ? "ACTIVATE" : "DEACTIVATE";
        auditService.log(id, user.getUsername(), action, "USER", id, "User " + action.toLowerCase() + "d");
        return toResponse(user);
    }

    private User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .active(user.isActive())
            .roles(user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet()))
            .createdAt(user.getCreatedAt())
            .build();
    }
}
