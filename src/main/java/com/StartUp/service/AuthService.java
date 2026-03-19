package com.StartUp.service;

import com.StartUp.dtos.auth.AuthDtos;
import com.StartUp.entity.EmployerProfile;
import com.StartUp.entity.StudentProfile;
import com.StartUp.entity.User;
import com.StartUp.enums.Role;
import com.StartUp.enums.UserStatus;
import com.StartUp.exception.AppExceptions;
import com.StartUp.repository.EmployerProfileRepository;
import com.StartUp.repository.StudentProfileRepository;
import com.StartUp.repository.UserRepository;
import com.StartUp.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

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
    private final EmailService emailService;
    private final ReferralService referralService;

    @Transactional
    public String register (AuthDtos.RegisterRequest request){
        if(userRepository.existsByEmail(request.email())){
            throw new AppExceptions.EmailAlreadyExistsException("Имейлът вече е регистриран.");
        }

        if (request.role() == Role.STUDENT) {
            if (request.university() == null || request.university().isBlank()) {
                throw new AppExceptions.BadRequestException("University is required for students");
            }
            if (request.major() == null || request.major().isBlank()) {
                throw new AppExceptions.BadRequestException("Major is required for students");
            }
        }

        if (request.role() == Role.EMPLOYER) {
            if (request.companyName() == null || request.companyName().isBlank()) {
                throw new AppExceptions.BadRequestException("Company name is required for employers");
            }
        }

        if(request.role() == Role.ADMIN){
            throw new AppExceptions.BadRequestException("Не може да се регистрирате като админ");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(request.role())
                .enabled(false)
                .phoneNumber(request.phoneNumber())
                .city(request.city())
                .status(UserStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);

        user = userRepository.save(user);

        String refCode = request.referralCode();
        if (refCode != null && !refCode.isBlank()) {
            referralService.handleRegistrationWithCode(refCode, user);
        }

        emailService.sendVerificationEmail(user.getEmail(), token);

        createEmptyProfile(user,request);

        return "Registration successful! Please check your email to verify your account.";
    }

    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppExceptions.InvalidTokenException("Потребителят не е намерен"));

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new AppExceptions.AccountBlockedException();
        }

        if (!user.isEnabled()) {
            throw new AppExceptions.BadRequestException("Трябва да си верифицирате акаунта");
        }

        return buildTokenResponse(user);
    }

    @Transactional
    public AuthDtos.AuthResponse refreshToken(AuthDtos.RefreshTokenRequest request){
        final String  userEmail = jwtService.extractUsername(request.refreshToken());

        if(userEmail == null){
            throw new AppExceptions.InvalidTokenException("Невалиден Refresh Token");
        }

        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new AppExceptions.ResourceNotFoundException("Потребителят не е намерен"));

        if(!request.refreshToken().equals(user.getRefreshToken())){
            throw new AppExceptions.InvalidTokenException("Refresh Token не съвпада");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        if(!jwtService.isTokenValid(request.refreshToken(),userDetails)){
            throw new AppExceptions.InvalidTokenException("Невалиден токен");
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



    private void createEmptyProfile(User user, AuthDtos.RegisterRequest request){
        if (user.getRole() == Role.STUDENT) {
            StudentProfile profile = StudentProfile.builder()
                    .user(user)
                    .city(user.getCity())
                    .createdAt(LocalDateTime.now())
                    .phone(user.getPhoneNumber())
                    .country(request.country())
                    .githubUrl(request.github())
                    .dateOfBirth(request.dateOfBirth())
                    .major(request.major())
                    .university(request.university())
                    .linkedinUrl(request.linkedin())
                    .university(request.university())
                    .application(new ArrayList<>())
                    .build();
            studentProfileRepository.save(profile);

        } else if (user.getRole() == Role.EMPLOYER) {
            EmployerProfile profile = EmployerProfile.builder()
                    .user(user)
                    .city(user.getCity())
                    .createdAt(LocalDateTime.now())
                    .isVerified(user.isEnabled())
                    .website(request.website())
                    .phone(user.getPhoneNumber())
                    .country(request.country())
                    .companyName(request.companyName() != null ? request.companyName() : user.getFirstName() + "'s Company")
                    .website(request.website())
                    .createdAt(LocalDateTime.now())
                    .build();
            employerProfileRepository.save(profile);
        }
    }
}
