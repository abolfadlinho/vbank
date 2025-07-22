package com.vbank.userservice.controller;

import com.vbank.userservice.dto.UserDto;
import com.vbank.userservice.model.User;
import com.vbank.userservice.service.UserService;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // --- DTOs for Request Payloads defined as Records ---

    // Record for user registration request payload
    public record UserRegistrationRequest(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 100) String password,
            @NotBlank String firstName,
            @NotBlank String lastName
    ) {}

    // Record for user login request payload
    public record UserLoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {}

    // --- Endpoints ---

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        UserDto createdUserDto = userService.registerUser(user);
        return new ResponseEntity<>(createdUserDto, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> loginUser(@Valid @RequestBody UserLoginRequest request) {
        UserDto userDto = userService.loginUser(request.username(), request.password());
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserDto> getUserProfile(@PathVariable UUID userId) {
        UserDto userDto = userService.getUserProfile(userId);
        return ResponseEntity.ok(userDto);
    }
}