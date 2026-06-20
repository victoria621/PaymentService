package com.example.paymentservice.service;

import com.example.paymentservice.dto.PaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;
import com.example.paymentservice.entity.PaymentEntity;
import com.example.paymentservice.mapper.PaymentMapper;
import com.example.paymentservice.repository.CustomPaymentRepository;
import com.example.paymentservice.repository.PaymentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RestTemplate restTemplate;
    private final CustomPaymentRepository customPaymentRepository;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest paymentRequest) {
        PaymentEntity  paymentEntity = paymentMapper.toEntity(paymentRequest);
        paymentEntity.setTimestamp(Instant.now());
        paymentEntity.setStatus("PENDING");

        paymentEntity = paymentRepository.save(paymentEntity);
        int randomNumber = fetchRandomNumber();

        if(randomNumber % 2 ==0) {
            paymentEntity.setStatus("SUCCESS");
        } else{
            paymentEntity.setStatus("FAILED");
        }

        paymentEntity = paymentRepository.save(paymentEntity);
        return paymentMapper.toDto(paymentEntity);

    }

    private int fetchRandomNumber() {
        String url = "https://www.random.org/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new";

        try {
            String response = restTemplate.getForObject(url, String.class);
            return Integer.parseInt(response.trim());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get random number from external API", e);
        }
    }

    public List<PaymentResponse> getPaymentsByCriteria(Long userId, String orderId, String status) {
        List<PaymentEntity> paymentEntity = customPaymentRepository.findPaymentsByCriteria(userId, orderId, status);
        return paymentEntity.stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }

    public BigDecimal getUserSum(Long userId, Instant start, Instant end) {
        return customPaymentRepository.sumPaymentsByCriteria(userId, start, end);
    }

    public BigDecimal getAllUserSum(Instant start,Instant end) {
        return customPaymentRepository.sumPaymentsByCriteria(null,start,end);
    }
}
