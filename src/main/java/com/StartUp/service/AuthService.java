package com.StartUp.service;

import com.StartUp.dtos.auth.AuthDtos;
import com.StartUp.entity.EmployerProfile;
import com.StartUp.entity.StudentProfile;
import com.StartUp.entity.User;
import com.StartUp.enums.Role;
import com.StartUp.enums.UserStatus;
import com.StartUp.repository.EmployerProfileRepository;
import com.StartUp.repository.StudentProfileRepository;
import com.StartUp.repository.UserRepository;
import com.StartUp.security.JwtService;
import com.StartUp.dtos.auth.AuthDtos;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Transactional
    public AuthDtos.AuthResponse register (AuthDtos.RegisterRequest request){
        if(userRepository.existsByEmail(request.email())){

        }

        if(request.role() == Role.ADMIN){

        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(request.role())
                .status(UserStatus.PENDING)
                .build();

        user = userRepository.save(user);

        createEmptyProfile(user);

        return buildTokenResponse(user);
    }

    @Transactional
    public AuthDtos.AuthResponse login (AuthDtos.LoginRequest request){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(),request.password()));

        // TODO: CHANGE THE EXCEPTION LATER WITH GLOBAL EXCEPTION
        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new RuntimeException("Потребителят не е намерен"));

        // TODO: CHANGE THE EXCEPTION LATER WITH GLOBAL EXCEPTION
        if(user.getStatus() == UserStatus.BLOCKED){
            throw new RuntimeException("Потребителят е блокиран");
        }

        return buildTokenResponse(user);
    }

    @Transactional
    public AuthDtos.AuthResponse refreshToken(AuthDtos.RefreshTokenRequest request){
        final String  userEmail = jwtService.extractUsername(request.refreshToken());

        // TODO: CHANGE THE EXCEPTION LATER WITH GLOBAL EXCEPTION
        if(userEmail == null){
            throw new RuntimeException("Невалиден Refresh Token");
        }

        // TODO: CHANGE THE EXCEPTION LATER WITH GLOBAL EXCEPTION
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("Потребителят не е намерен"));

        if(!request.refreshToken().equals(user.getRefreshToken())){
            // TODO: CHANGE THE EXCEPTION LATER WITH GLOBAL EXCEPTION
            throw new RuntimeException("Refresh Token не съвпада");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        if(!jwtService.isTokenValid(request.refreshToken(),userDetails)){
            // TODO: CHANGE THE EXCEPTION LATER WITH GLOBAL EXCEPTION
            throw new RuntimeException("Невалиден токен");
        }

        return buildTokenResponse(user);

    }

    @Transactional
    public void logout(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setRefreshToken(null);
            userRepository.save(user);
        });
    }

    private AuthDtos.AuthResponse buildTokenResponse(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new AuthDtos.AuthResponse(accessToken, refreshToken, user.getEmail(), user.getRole().name());
    }



    private void createEmptyProfile(User user){
        if(user.getRole() == Role.STUDENT){
            StudentProfile profile = StudentProfile.builder().user(user).build();
            studentProfileRepository.save(profile);
        } else if(user.getRole() == Role.EMPLOYER){
            EmployerProfile profile = EmployerProfile.builder()
                    .user(user)
                    .companyName(user.getFirstName() + "'s Company")
                    .build();
            employerProfileRepository.save(profile);
        }
    }
}
