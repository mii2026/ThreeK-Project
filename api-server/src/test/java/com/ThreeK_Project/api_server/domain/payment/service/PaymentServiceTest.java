package com.ThreeK_Project.api_server.domain.payment.service;

import com.ThreeK_Project.api_server.domain.order.entity.Order;
import com.ThreeK_Project.api_server.domain.payment.dto.RequestDto.PaymentRequestDto;
import com.ThreeK_Project.api_server.domain.payment.dto.ResponseDto.PaymentResponseDto;
import com.ThreeK_Project.api_server.domain.payment.dto.RequestDto.PaymentSearchDto;
import com.ThreeK_Project.api_server.domain.payment.dto.RequestDto.PaymentUpdateDto;
import com.ThreeK_Project.api_server.domain.payment.entity.Payment;
import com.ThreeK_Project.api_server.domain.payment.enums.PaymentMethod;
import com.ThreeK_Project.api_server.domain.payment.enums.PaymentStatus;
import com.ThreeK_Project.api_server.domain.payment.repository.PaymentRepository;
import com.ThreeK_Project.api_server.domain.user.entity.User;
import com.ThreeK_Project.api_server.domain.user.enums.Role;
import com.ThreeK_Project.api_server.global.exception.ApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private User user;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("결제 정보 생성 성공 테스트?")
    public void createPaymentTest() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        PaymentRequestDto requestDto = new PaymentRequestDto(
                PaymentMethod.CARD, new BigDecimal(10000));

        paymentService.createPayment(order, requestDto);
    }

    @Test
    @DisplayName("결제 정보 수정 성공 테스트")
    public void updatePaymentTest() {
        UUID paymentId = UUID.randomUUID();
        Payment payment = new Payment();
        PaymentUpdateDto paymentUpdateDto = new PaymentUpdateDto(
                PaymentMethod.CARD, PaymentStatus.FAIL, new BigDecimal(10000)
        );

        doReturn(Optional.of(payment))
                .when(paymentRepository)
                .findById(paymentId);

        paymentService.updatePayment(paymentId, paymentUpdateDto);
    }

    @Test
    @DisplayName("결제 정보 수정 실패 테스트 - 결제 정보 없음")
    public void updatePaymentTest2() {
        UUID paymentId = UUID.randomUUID();
        PaymentUpdateDto paymentUpdateDto = new PaymentUpdateDto(
                PaymentMethod.CARD, PaymentStatus.FAIL, new BigDecimal(10000)
        );

        doReturn(Optional.empty())
                .when(paymentRepository)
                .findById(paymentId);

        ApplicationException e = Assertions.
                assertThrows(ApplicationException.class, () -> paymentService.updatePayment(paymentId, paymentUpdateDto));
        assertThat(e.getMessage()).isEqualTo("Payment not found");
    }

    @Test
    @DisplayName("결제 정보 조회 성공 테스트")
    public void getPaymentTest() {
        UUID paymentId = UUID.randomUUID();
        Payment payment = Payment.createPayment(
                PaymentMethod.CARD, PaymentStatus.SUCCESS, new BigDecimal(10000), new Order());
        List<Role> roles = new ArrayList<>();
        roles.add(Role.MANAGER);

        doReturn(Optional.of(payment))
                .when(paymentRepository)
                .findById(paymentId);

        doReturn(roles)
                .when(user)
                .getRoles();

        PaymentResponseDto responseDto = paymentService.getPayment(user, paymentId);
        assertEquals(responseDto.getPaymentMethod(), PaymentMethod.CARD);
        assertEquals(responseDto.getPaymentStatus(), PaymentStatus.SUCCESS);
        assertEquals(responseDto.getAmount(), new BigDecimal(10000));
    }

    @Test
    @DisplayName("결제 정보 조회 실패 테스트 - 결제 정보 없음")
    public void getPaymentTest2() {
        UUID paymentId = UUID.randomUUID();

        doReturn(Optional.empty())
                .when(paymentRepository)
                .findById(paymentId);

        ApplicationException e = Assertions.
                assertThrows(ApplicationException.class, () -> paymentService.getPayment(user, paymentId));
        assertThat(e.getMessage()).isEqualTo("Payment not found");
    }

    @Test
    @DisplayName("사용자 주문 검색 성공 테스트")
    public void searchUserOrdersTest(){
        String username = "test";
        PaymentSearchDto paymentSearchDto = new PaymentSearchDto();
        paymentSearchDto.setUsername(username);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "created_at"));
        List<Payment> payments = new ArrayList<>();
        Page<Payment> pages = new PageImpl<>(payments, pageable, 0);

        doReturn(pages)
                .when(paymentRepository)
                .searchPayments(pageable, paymentSearchDto);

        Page<PaymentResponseDto> result = paymentService.searchUserPayments(username, paymentSearchDto);
        assertEquals(result.getTotalElements(), 0);
    }

    @Test
    @DisplayName("사용자 주문 검색 실패 테스트 - 다른 사용자 정보 조회")
    public void searchUserOrdersTest2(){
        String username = "test1";
        PaymentSearchDto paymentSearchDto = new PaymentSearchDto();
        paymentSearchDto.setUsername("test2");

        ApplicationException e = Assertions.
                assertThrows(ApplicationException.class, () -> paymentService.searchUserPayments(username, paymentSearchDto));
        assertThat(e.getMessage()).isEqualTo("Invalid user");
    }

    @Test
    @DisplayName("가게 주인 주문 검색 실패 테스트 - 다른 가게 정보 조회")
    public void searchRestaurantOrdersTest(){
        String restaurantName = "test1";
        PaymentSearchDto paymentSearchDto = new PaymentSearchDto();
        paymentSearchDto.setRestaurantName("test2");

        ApplicationException e = Assertions.
                assertThrows(ApplicationException.class, () -> paymentService.searchRestaurantPayments(restaurantName, paymentSearchDto));
        assertThat(e.getMessage()).isEqualTo("Invalid restaurant");
    }

    @Test
    @DisplayName("결제 정보 삭제 성공 테스트")
    public void deletePaymentTest() {
        UUID paymentId = UUID.randomUUID();

        doReturn(Optional.of(new Payment()))
                .when(paymentRepository)
                .findById(paymentId);

        paymentService.deletePayment(paymentId, new User());
    }

    @Test
    @DisplayName("결제 정보 삭제 실패 테스트 - 결제 정보 없음")
    public void deletePaymentTest2() {
        UUID paymentId = UUID.randomUUID();

        doReturn(Optional.empty())
                .when(paymentRepository)
                .findById(paymentId);

        ApplicationException e = Assertions.
                assertThrows(ApplicationException.class, () -> paymentService.deletePayment(paymentId, new User()));
        assertThat(e.getMessage()).isEqualTo("Payment not found");
    }
}