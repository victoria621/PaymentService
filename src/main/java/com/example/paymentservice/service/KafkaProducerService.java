package com.example.paymentservice.service;

import com.example.paymentservice.dto.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class KafkaProducerService {
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    public void sendPaymentEvent(PaymentEvent paymentEvent) {
        try {
            kafkaTemplate.send("payment-events", paymentEvent);
            log.info("Payment event sent: orderId={}, status={}",
                    paymentEvent.getOrderId(), paymentEvent.getStatus());
        } catch (Exception e) {
            log.error("Failed to send payment event: {}", paymentEvent, e);
        }
    }
}
