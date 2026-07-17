package com.taskflow.controller;

import com.taskflow.config.UserPrincipal;
import com.taskflow.dto.UserResponse;
import com.taskflow.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/users")
@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        // سحر! جبنا الـ ID من غير ما نروح الداتا بيز!
        Long userId = userPrincipal.getId();
        String username = userPrincipal.getUsername();
        String email = userPrincipal.getEmail();

        return ResponseEntity.ok(new UserResponse(userId, username, email));
    }
}
