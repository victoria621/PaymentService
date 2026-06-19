package com.example.paymentservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String id,
        Long userId,
        Long orderId,
        String status,
        Instant timestamp,
        BigDecimal paymentAmount
) {
}
