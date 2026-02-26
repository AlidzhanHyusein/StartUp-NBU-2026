package com.StartUp.dtos.auth;

import com.StartUp.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data

public class AuthDtos {

    public record RegisterRequest(
            @NotBlank @Email
            String email,

            @NotBlank @Size(min = 8, message = "Паролата трябва да е поне 8 символа")
            String password,

            @NotBlank
            String firstName,

            @NotBlank
            String lastName,

            @NotNull
            Role role
    ) {}

    public record LoginRequest(
            @NotBlank @Email
            String email,

            @NotBlank
            String password
    ) {}

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            String email,
            String role
    ) {}
    public record RefreshTokenRequest(
            @NotBlank
            String refreshToken
    ) {}
}
