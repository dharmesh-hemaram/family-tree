package com.familytree.service;

import com.familytree.exception.ResourceAlreadyExistsException;
import com.familytree.exception.ResourceNotFoundException;
import com.familytree.model.User;
import com.familytree.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Service for managing users and authentication.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Transactional
    public User createUser(String username, String email, String password, Set<String> roles) {
        if (userRepository.existsByUsername(username)) {
            throw new ResourceAlreadyExistsException("User", username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("User with email", email);
        }
        
        User user = User.builder()
            .username(username)
            .email(email)
            .passwordHash(passwordEncoder.encode(password))
            .roles(roles != null ? roles : new HashSet<>())
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .build();
        
        return userRepository.save(user);
    }
    
    @Transactional
    public void updateLastLogin(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User with username: " + username));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    @Transactional
    public void addRole(Long userId, String role) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.getRoles().add(role);
        userRepository.save(user);
    }
    
    @Transactional
    public void removeRole(Long userId, String role) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.getRoles().remove(role);
        userRepository.save(user);
    }
}
