package com.example.paymentservice.controller;

import com.example.paymentservice.dto.PaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;
import com.example.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        log.info("Creating payment for userId: {}, orderId: {}, amount: {}",
                paymentRequest.userId(), paymentRequest.orderId(), paymentRequest.paymentAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(paymentRequest));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getPaymentByCriteria(@RequestParam(required = false) Long userId,
                                                                      @RequestParam(required = false) String orderId,
                                                                      @RequestParam(required = false) String status
    ) {
        log.info("Getting payments by criteria: userId={}, orderId={}, status={}", userId, orderId, status);

        return ResponseEntity.ok(paymentService.getPaymentsByCriteria(userId,orderId,status));
    }

    @GetMapping("/sum/me")
    public ResponseEntity<BigDecimal> getUserSum(@RequestParam Long userId,
                                                 @RequestParam Instant start,
                                                 @RequestParam Instant end
    ) {

        log.info("Getting sum for user: {}, from {} to {}", userId, start, end);

        return ResponseEntity.ok(paymentService.getUserSum(userId, start, end));
    }

    @GetMapping("/sum/all")
    public ResponseEntity<BigDecimal> getAllUserSum(@RequestParam Instant start,
                                                    @RequestParam Instant end
    ) {

        log.info("Getting total sum for all users, from {} to {}", start, end);
        return ResponseEntity.ok(paymentService.getAllUserSum(start, end));
    }
}
