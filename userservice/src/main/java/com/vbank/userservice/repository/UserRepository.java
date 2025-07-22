package com.vbank.userservice.repository;

import com.vbank.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by their username.
     * @param username The username to search for.
     * @return An Optional containing the user if found.
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their email address.
     * @param email The email to search for.
     * @return An Optional containing the user if found.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given username.
     * @param username The username to check.
     * @return true if a user with the username exists, false otherwise.
     */
    boolean existsByUsername(String username);

    /**
     * Checks if a user exists with the given email.
     * @param email The email to check.
     * @return true if a user with the email exists, false otherwise.
     */
    boolean existsByEmail(String email);
}