package com.taskflow.dto;

import com.taskflow.entity.Roles;

public record UserResponse(Long id, String username, String email, String role) {
}
