package com.hotel.reservation.service;

import com.hotel.reservation.dto.response.UserResponseDto;
import com.hotel.reservation.exception.ResourceNotFoundException;
import com.hotel.reservation.mapper.UserMapper;
import com.hotel.reservation.model.User;
import com.hotel.reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Spring Security UserDetailsService implementation
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public UserResponseDto getUserByUsername(String username) {
        log.info("Getting user by username: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> ResourceNotFoundException.userByUsername(username));
                
        return userMapper.toResponseDto(user);
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(String userId) {
        log.info("Getting user by ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.user(userId));
                
        return userMapper.toResponseDto(user);
    }

    /**
     * Get all staff members for check-in/out operations
     */
    @Transactional(readOnly = true)
    public List<UserResponseDto> getActiveStaffMembers() {
        log.info("Getting active staff members");
        
        List<User> staffMembers = userRepository.findActiveStaffMembers();
        
        return staffMembers.stream()
                .map(userMapper::toResponseDto)
                .toList();
    }

    /**
     * Search users for staff operations
     */
    @Transactional(readOnly = true)
    public List<UserResponseDto> searchUsers(String searchTerm) {
        log.info("Searching users with term: {}", searchTerm);
        
        List<User> users = userRepository.searchUsers(searchTerm);
        
        return users.stream()
                .map(userMapper::toResponseDto)
                .toList();
    }

    /**
     * Unlock user account (for admin operations)
     */
    @Transactional
    public void unlockUserAccount(String username) {
        log.info("Unlocking user account: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> ResourceNotFoundException.userByUsername(username));
        
        user.setAccountNonLocked(true);
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        
        userRepository.save(user);
        
        log.info("User account unlocked successfully: {}", username);
    }

    /**
     * Process expired account lockouts (scheduled task)
     */
    @Transactional
    public void processExpiredLockouts() {
        LocalDateTime now = LocalDateTime.of(2025, 11, 22, 4, 14, 31);
        List<User> expiredLockouts = userRepository.findAccountsWithExpiredLockouts(now);
        
        for (User user : expiredLockouts) {
            user.setAccountNonLocked(true);
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            
            log.info("Automatically unlocked expired lockout for user: {}", user.getUsername());
        }
    }
}