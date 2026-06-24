package com.example.paymentservice.dto;

import com.example.paymentservice.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String id,
        Long userId,
        Long orderId,
        PaymentStatus status,
        Instant timestamp,
        BigDecimal paymentAmount
) {
}
