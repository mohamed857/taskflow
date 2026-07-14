package com.taskflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email
) {
}
