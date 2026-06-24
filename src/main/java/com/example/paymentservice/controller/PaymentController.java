    package com.example.paymentservice.controller;

    import com.example.paymentservice.dto.PaymentRequest;
    import com.example.paymentservice.dto.PaymentResponse;
    import com.example.paymentservice.entity.PaymentStatus;
    import com.example.paymentservice.service.PaymentService;
    import jakarta.validation.Valid;
    import lombok.AllArgsConstructor;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.prepost.PreAuthorize;
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
        @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
        public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
            log.info("Creating payment for userId: {}, orderId: {}, amount: {}",
                    paymentRequest.userId(), paymentRequest.orderId(), paymentRequest.paymentAmount());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(paymentService.createPayment(paymentRequest));
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
        public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable String id) {
            log.info("Getting payment by id: {}", id);
            return ResponseEntity.ok(paymentService.getPaymentById(id));
        }

        @GetMapping
        @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
        public ResponseEntity<List<PaymentResponse>> getPaymentByCriteria(@RequestParam(required = false) Long userId,
                                                                          @RequestParam(required = false) String orderId,
                                                                          @RequestParam(required = false) String status
        ) {
            log.info("Getting payments by criteria: userId={}, orderId={}, status={}", userId, orderId, status);

            PaymentStatus statusEnum = null;
            if (status != null && !status.isEmpty()) {
                statusEnum = PaymentStatus.valueOf(status);
            }
            return ResponseEntity.ok(paymentService.getPaymentsByCriteria(userId,orderId,statusEnum));
        }

        @GetMapping("/users/{user_id}/summary")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
        public ResponseEntity<BigDecimal> getUserSum(@PathVariable("user_id") Long userId,
                                                     @RequestParam Instant start,
                                                     @RequestParam Instant end
        ) {

            log.info("Getting sum for user: {}, from {} to {}", userId, start, end);

            return ResponseEntity.ok(paymentService.getUserSum(userId, start, end));
        }

        @GetMapping("/summary")
        @PreAuthorize("hasAnyRole('ADMIN')")
        public ResponseEntity<BigDecimal> getAllUserSum(@RequestParam Instant start,
                                                        @RequestParam Instant end
        ) {

            log.info("Getting total sum for all users, from {} to {}", start, end);
            return ResponseEntity.ok(paymentService.getAllUserSum(start, end));
        }
    }
