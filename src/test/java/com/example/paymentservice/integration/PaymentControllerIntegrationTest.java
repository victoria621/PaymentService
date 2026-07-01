package com.example.paymentservice.integration;

import com.example.paymentservice.dto.PaymentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PaymentControllerIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "USER")
    void createPayment_ShouldReturn202Accepted() throws Exception {
        PaymentRequest request = new PaymentRequest(1L, 1L, BigDecimal.valueOf(100.00));

        MvcResult result = mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        assertThat(responseJson).contains("id");
    }

    @Test
    @WithMockUser(roles = "USER")
    void createPayment_ShouldReturnBadRequest_WhenInvalidPayload() throws Exception {
        PaymentRequest request = new PaymentRequest(null, null, null);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getPaymentById_ShouldReturnPayment_WhenExists() throws Exception {
        PaymentRequest request = new PaymentRequest(1L, 2L, BigDecimal.valueOf(200.00));
        MvcResult createResult = mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        String id = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(get("/api/payments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.orderId").value(2L));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getPaymentById_ShouldReturn404_WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/payments/{id}", "non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getPaymentsByCriteria_ShouldReturnFilteredPayments() throws Exception {
        PaymentRequest request1 = new PaymentRequest(2L, 3L, BigDecimal.valueOf(50.00));
        PaymentRequest request2 = new PaymentRequest(2L, 4L, BigDecimal.valueOf(75.00));
        PaymentRequest request3 = new PaymentRequest(3L, 5L, BigDecimal.valueOf(100.00));

        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1))).andExpect(status().isAccepted());
        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2))).andExpect(status().isAccepted());
        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request3))).andExpect(status().isAccepted());

        mockMvc.perform(get("/api/payments")
                        .param("userId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getPaymentsByCriteria_ShouldReturnEmptyList_WhenNoMatches() throws Exception {
        mockMvc.perform(get("/api/payments")
                        .param("userId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserSum_ShouldReturnTotalSumForUser() throws Exception {
        PaymentRequest request1 = new PaymentRequest(10L, 6L, BigDecimal.valueOf(100.00));
        PaymentRequest request2 = new PaymentRequest(10L, 7L, BigDecimal.valueOf(200.00));

        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1))).andExpect(status().isAccepted());
        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2))).andExpect(status().isAccepted());

        mockMvc.perform(get("/api/payments/users/{user_id}/summary", 10)
                        .param("start", "2000-01-01T00:00:00Z")
                        .param("end", "2100-01-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber());
    }
}