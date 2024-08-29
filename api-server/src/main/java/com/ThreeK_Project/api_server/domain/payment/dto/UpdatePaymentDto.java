package com.ThreeK_Project.api_server.domain.payment.dto;

import com.ThreeK_Project.api_server.domain.payment.enums.PaymentMethod;
import com.ThreeK_Project.api_server.domain.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentDto {
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private BigDecimal amount;
}
