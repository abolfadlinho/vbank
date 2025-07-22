package com.vbank.userservice.service;

import com.vbank.userservice.dto.UserDto;
import com.vbank.userservice.model.User;
import com.vbank.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

/**
 * Service layer containing business logic for user operations.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user.
     * @param user The user object containing registration details.
     * @return A DTO of the newly created user.
     */
    public UserDto registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken.");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        return convertToDto(savedUser);
    }

    /**
     * Authenticates a user.
     * @param username The user's username.
     * @param password The user's raw password.
     * @return A DTO of the authenticated user.
     */
    public UserDto loginUser(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
        }

        return convertToDto(user);
    }

    /**
     * Retrieves a user's profile by their ID.
     * @param userId The UUID of the user.
     * @return A DTO of the user.
     */
    public UserDto getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userId));

        return convertToDto(user);
    }

    /**
     * Helper method to convert a User entity to a UserDto.
     * @param user The User entity.
     * @return The corresponding UserDto.
     */
    private UserDto convertToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}