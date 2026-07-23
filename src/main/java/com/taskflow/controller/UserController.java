package com.taskflow.controller;

import com.taskflow.config.UserPrincipal;
import com.taskflow.dto.UserResponse;
import com.taskflow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/users")
@RestController
@SecurityRequirement(name = "bearerAuth")

public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        String username = userPrincipal.getUsername();
        String email = userPrincipal.getEmail();
        String role = userPrincipal.getRole();

        return ResponseEntity.ok(new UserResponse(userId, username, email,role));
    }
    @GetMapping
    @Operation(summary = "Get all users (Useful for assigning tasks)")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
