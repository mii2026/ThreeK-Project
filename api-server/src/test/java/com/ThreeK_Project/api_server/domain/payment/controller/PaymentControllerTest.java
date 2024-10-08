package com.ThreeK_Project.api_server.domain.payment.controller;

import com.ThreeK_Project.api_server.customMockUser.WithCustomMockUser;
import com.ThreeK_Project.api_server.domain.order.entity.Order;
import com.ThreeK_Project.api_server.domain.payment.dto.ResponseDto.PaymentResponseDto;
import com.ThreeK_Project.api_server.domain.payment.dto.RequestDto.PaymentUpdateDto;
import com.ThreeK_Project.api_server.domain.payment.entity.Payment;
import com.ThreeK_Project.api_server.domain.payment.enums.PaymentMethod;
import com.ThreeK_Project.api_server.domain.payment.enums.PaymentStatus;
import com.ThreeK_Project.api_server.domain.payment.service.PaymentService;
import com.ThreeK_Project.api_server.domain.user.entity.User;
import com.ThreeK_Project.api_server.global.security.auth.UserDetailsCustom;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
class PaymentControllerTest {
    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
    }

    @Test
    @DisplayName("결제 정보 조회 성공 테스트")
    @WithCustomMockUser
    public void getPaymentTest() throws Exception {
        UUID paymentId = UUID.randomUUID();
        UserDetailsCustom userDetails = (UserDetailsCustom) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userDetails.getUser();
        Payment payment = Payment.createPayment(
                PaymentMethod.CARD, PaymentStatus.SUCCESS, new BigDecimal(10000), new Order());
        PaymentResponseDto responseDto = new PaymentResponseDto(payment);

        doReturn(responseDto)
                .when(paymentService)
                .getPayment(user, paymentId);

        mockMvc.perform(get("/api/payments/" + paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("paymentStatus").value("SUCCESS"))
                .andExpect(jsonPath("paymentMethod").value("CARD"))
                .andExpect(jsonPath("amount").value(10000));
    }
}