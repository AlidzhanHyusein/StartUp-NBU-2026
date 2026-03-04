package com.StartUp.controller;

import com.StartUp.dtos.auth.AuthDtos;
import com.StartUp.entity.User;
import com.StartUp.enums.Role;
import com.StartUp.repository.UserRepository;
import com.StartUp.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthDtos.AuthResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest request){
        if(request.role() == Role.ADMIN){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        user.setEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        return ResponseEntity.ok("Email verified! You can now log in.");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDtos.AuthResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request){
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthDtos.AuthResponse> refresh(@Valid @RequestBody AuthDtos.RefreshTokenRequest request){
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthDtos.AuthResponse> logout(@AuthenticationPrincipal UserDetails userDetails){
        authService.logout(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
