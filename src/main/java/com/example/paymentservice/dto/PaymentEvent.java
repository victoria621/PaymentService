package com.example.paymentservice.dto;

import com.example.paymentservice.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent {
    private String id;
    private Long orderId;
    private PaymentStatus status;
    private Instant timestamp;
}
