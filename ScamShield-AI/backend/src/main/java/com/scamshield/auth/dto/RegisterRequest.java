package com.scamshield.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterRequest(
        @NotBlank(message = "Full name is required.")
        @Size(max = 100, message = "Full name must be 100 characters or fewer.")
        String fullName,

        @NotBlank(message = "Email is required.")
        @Email(message = "Enter a valid email address.")
        @Size(max = 254, message = "Email must be 254 characters or fewer.")
        String email,

        @NotBlank(message = "Password is required.")
        @Size(min = 12, max = 72, message = "Password must be between 12 and 72 characters.")
        String password,

        @NotBlank(message = "Password confirmation is required.")
        String confirmPassword) {
}
