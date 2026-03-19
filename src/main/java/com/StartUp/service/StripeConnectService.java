package com.StartUp.service;

import com.StartUp.entity.User;
import com.StartUp.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.Transfer;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.TransferCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class StripeConnectService {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.connect.refresh-url}")
    private String refreshUrl;

    @Value("${stripe.connect.return-url}")
    private String returnUrl;

    private final UserRepository userRepository;

    public String createConnectAccount(User user) throws Exception {
        Stripe.apiKey = secretKey;

        if (user.getStripeAccountId() != null) {
            return createOnboardingLink(user.getStripeAccountId());
        }

        AccountCreateParams params = AccountCreateParams.builder()
                .setType(AccountCreateParams.Type.EXPRESS)
                .setEmail(user.getEmail())
                .setCapabilities(AccountCreateParams.Capabilities.builder()
                        .setTransfers(AccountCreateParams.Capabilities.Transfers.builder()
                                .setRequested(true)
                                .build())
                        .build())
                .build();

        Account account = Account.create(params);

        user.setStripeAccountId(account.getId());
        userRepository.save(user);

        return createOnboardingLink(account.getId());
    }

    private String createOnboardingLink(String accountId) throws Exception {
        Stripe.apiKey = secretKey;

        AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                .setAccount(accountId)
                .setRefreshUrl(refreshUrl)
                .setReturnUrl(returnUrl)
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build();

        AccountLink accountLink = AccountLink.create(params);
        return accountLink.getUrl();
    }

    public void transferToStudent(User student, BigDecimal amount) throws Exception {
        Stripe.apiKey = secretKey;

        if (student.getStripeAccountId() == null) {
            throw new RuntimeException("Student has not connected their bank account yet");
        }

        if (!Boolean.TRUE.equals(student.getStripeOnboardingComplete())) {
            throw new RuntimeException("Student has not completed Stripe onboarding");
        }

        long amountInCents = amount.multiply(new BigDecimal("100")).longValue();

        TransferCreateParams params = TransferCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("eur")
                .setDestination(student.getStripeAccountId())
                .build();

        Transfer.create(params);
    }

    public void markOnboardingComplete(String stripeAccountId) {
        userRepository.findByStripeAccountId(stripeAccountId).ifPresent(user -> {
            user.setStripeOnboardingComplete(true);
            userRepository.save(user);
        });
    }
}