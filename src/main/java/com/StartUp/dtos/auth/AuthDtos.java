package com.StartUp.dtos.auth;

import com.StartUp.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

@Data

public class AuthDtos {

    public record RegisterRequest(

            @Email(message = "Invalid email format")
            @NotBlank(message = "Email is required")
            String email,

            @NotBlank(message = "Password is required")
            @Size(min = 8, message = "Password must be at least 8 characters")
            String password,

            @NotBlank(message = "First name is required")
            String firstName,

            @NotBlank(message = "Last name is required")
            String lastName,

            @NotNull(message = "Role is required")
            Role role,

            @NotBlank(message = "Phone number is required")
            String phoneNumber,

            @NotBlank(message = "City is required")
            String city,

            String github,
            String linkedin,
            String university,

            String companyName,
            String website
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
