package com.StartUp.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private Long jobApplicationId;
    private Long payerId;
    private Long receiverId;
    private BigDecimal amount;
    private String paymentMethod;
    private String notes;
}
