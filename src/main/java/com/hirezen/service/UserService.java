package com.hirezen.service;

import com.hirezen.model.Role;
import com.hirezen.model.User;
import com.hirezen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public User signup(String name, String email, String rawPassword, Role role) {
        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        User user = User.builder()
                .hirezenId(generateHirezenId(role))
                .name(name.trim())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {} ({}, {})", saved.getEmail(), saved.getRole(), saved.getHirezenId());
        return saved;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase()).orElse(null);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    @Transactional
    public User updateName(User user, String name) {
        user.setName(name.trim());
        return userRepository.save(user);
    }

    public List<User> search(String query) {
        return userRepository.findByHirezenIdIgnoreCaseOrNameContainingIgnoreCase(query, query);
    }

    public long totalUsersCount() {
        return userRepository.count();
    }

    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    private String generateHirezenId(Role role) {
        long countSoFar = userRepository.countByRole(role);
        return role.idPrefix() + (countSoFar + 1);
    }
}
