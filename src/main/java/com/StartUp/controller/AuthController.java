package com.StartUp.controller;

import com.StartUp.dtos.auth.AuthDtos;
import com.StartUp.entity.User;
import com.StartUp.enums.UserStatus;
import com.StartUp.repository.UserRepository;
import com.StartUp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, verify email, refresh and logout")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @Operation(summary = "Register a new user", description = "Creates a new STUDENT or EMPLOYER account and sends a verification email")
    @PostMapping("/register")
        public ResponseEntity<String> register(@Valid @RequestBody AuthDtos.RegisterRequest request){

        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Verify email address", description = "Activates the user account using the token sent to their email")
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        user.setEnabled(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationToken(null);
        userRepository.save(user);


        HttpHeaders headers = new HttpHeaders();
        headers.set("Verify Now", "https://breaddy.store/auth/login?verified=true");
        headers.setLocation(URI.create("https://breaddy.store/auth/login?verified=true"));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @Operation(summary = "Login", description = "Authenticates a verified user and returns an access token and refresh token")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDtos.LoginRequest request){
        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new RuntimeException("Такъв акаунт с този имейл не същества"));
        if(!user.isEnabled()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Трябва да си верифицирате акаунта");
        }
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Refresh access token", description = "Issues a new access token using a valid refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthDtos.AuthResponse> refresh(@Valid @RequestBody AuthDtos.RefreshTokenRequest request){
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @Operation(summary = "Logout", description = "Invalidates the current user's refresh token and ends their session")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserDetails userDetails){
        authService.logout(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}