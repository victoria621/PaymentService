package com.example.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull(message = "userId cannot be null")
        Long userId,
        @NotNull(message = "orderId cannot be null")
        Long orderId,
        @NotNull(message = "paymentAmount cannot be null")
        @Positive(message = "paymentAmount must be positive")
        BigDecimal paymentAmount
) {
}
