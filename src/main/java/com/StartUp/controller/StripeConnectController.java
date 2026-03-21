package com.StartUp.controller;

import com.StartUp.entity.User;
import com.StartUp.repository.UserRepository;
import com.StartUp.service.StripeConnectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/connect")
@RequiredArgsConstructor
public class StripeConnectController {

    private final StripeConnectService stripeConnectService;
    private final UserRepository userRepository;

    @PostMapping("/onboard")
    public ResponseEntity<Map<String, String>> onboard(
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        String onboardingUrl = stripeConnectService.createConnectAccount(user);

        return ResponseEntity.ok(Map.of("onboardingUrl", onboardingUrl));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        return ResponseEntity.ok(Map.of(
                "hasConnectedAccount", user.getStripeAccountId() != null,
                "onboardingComplete", Boolean.TRUE.equals(user.getStripeOnboardingComplete())
        ));
    }
}