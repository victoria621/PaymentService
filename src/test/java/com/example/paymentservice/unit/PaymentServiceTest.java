package com.example.paymentservice.unit;

import com.example.paymentservice.dto.PaymentEvent;
import com.example.paymentservice.dto.PaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;
import com.example.paymentservice.entity.PaymentEntity;
import com.example.paymentservice.entity.PaymentStatus;
import com.example.paymentservice.mapper.PaymentMapper;
import com.example.paymentservice.repository.CustomPaymentRepository;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.service.KafkaProducerService;
import com.example.paymentservice.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CustomPaymentRepository customPaymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest request;
    private PaymentEntity entity;
    private PaymentResponse successResponse;
    private PaymentResponse failedResponse;

    @BeforeEach
    void setUp() {
        request = new PaymentRequest(1L, 1L, BigDecimal.valueOf(100.00));

        entity = new PaymentEntity();
        entity.setId("payment-123");
        entity.setUserId(1L);
        entity.setOrderId(1L);
        entity.setPaymentAmount(BigDecimal.valueOf(100.00));
        entity.setStatus(PaymentStatus.PENDING);
        entity.setTimestamp(Instant.now());

        successResponse = new PaymentResponse(
                "payment-123",
                1L,
                1L,
                PaymentStatus.SUCCESS,
                Instant.now(),
                BigDecimal.valueOf(100.00)
        );

        failedResponse = new PaymentResponse(
                "payment-123",
                1L,
                1L,
                PaymentStatus.FAILED,
                Instant.now(),
                BigDecimal.valueOf(100.00)
        );
    }

    @Test
    void createPayment_ShouldCreateAndReturnPayment() {
        when(paymentMapper.toEntity(any(PaymentRequest.class))).thenReturn(entity);
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(entity);
        when(restTemplate.getForObject(anyString(), any())).thenReturn("42");
        when(paymentMapper.toDto(any(PaymentEntity.class))).thenReturn(successResponse);

        PaymentResponse result = paymentService.createPayment(request);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(PaymentStatus.SUCCESS);
        verify(paymentRepository, times(2)).save(any(PaymentEntity.class));
        verify(kafkaProducerService).sendPaymentEvent(any(PaymentEvent.class));
    }

    @Test
    void createPayment_ShouldSetFailedStatus_WhenRandomNumberIsOdd() {
        when(paymentMapper.toEntity(any(PaymentRequest.class))).thenReturn(entity);
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(entity);
        when(restTemplate.getForObject(anyString(), any())).thenReturn("7");
        when(paymentMapper.toDto(any(PaymentEntity.class))).thenReturn(failedResponse);

        PaymentResponse result = paymentService.createPayment(request);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void getPaymentById_ShouldReturnPayment_WhenExists() {
        when(paymentRepository.findById("payment-123")).thenReturn(Optional.of(entity));
        when(paymentMapper.toDto(entity)).thenReturn(successResponse);

        PaymentResponse result = paymentService.getPaymentById("payment-123");

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("payment-123");
    }

    @Test
    void getPaymentById_ShouldThrowException_WhenNotFound() {
        when(paymentRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentById("invalid-id"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment not found");
    }

    @Test
    void getPaymentsByCriteria_ShouldReturnFilteredPayments() {
        List<PaymentEntity> entities = List.of(entity);
        when(customPaymentRepository.findPaymentsByCriteria(1L, 1L, PaymentStatus.SUCCESS))
                .thenReturn(entities);
        when(paymentMapper.toDto(any(PaymentEntity.class))).thenReturn(successResponse);

        List<PaymentResponse> results = paymentService.getPaymentsByCriteria(1L, 1L, PaymentStatus.SUCCESS);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().userId()).isEqualTo(1L);
    }

    @Test
    void getUserSum_ShouldReturnTotalSum() {
        Instant start = Instant.now().minusSeconds(86400);
        Instant end = Instant.now();
        when(customPaymentRepository.sumPaymentsByCriteria(1L, start, end))
                .thenReturn(BigDecimal.valueOf(500.00));

        BigDecimal sum = paymentService.getUserSum(1L, start, end);

        assertThat(sum).isEqualByComparingTo(BigDecimal.valueOf(500.00));
    }

    @Test
    void getAllUserSum_ShouldReturnTotalSumForAllUsers() {
        Instant start = Instant.now().minusSeconds(86400);
        Instant end = Instant.now();
        when(customPaymentRepository.sumPaymentsByCriteria(null, start, end))
                .thenReturn(BigDecimal.valueOf(1000.00));

        BigDecimal sum = paymentService.getAllUserSum(start, end);

        assertThat(sum).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
    }
}