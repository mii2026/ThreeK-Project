package com.ThreeK_Project.api_server.domain.order.service;

import com.ThreeK_Project.api_server.domain.order.dto.RequestDto.OrderRequestDto;
import com.ThreeK_Project.api_server.domain.order.dto.RequestDto.OrderSearchDTO;
import com.ThreeK_Project.api_server.domain.order.dto.RequestDto.ProductRequestData;
import com.ThreeK_Project.api_server.domain.order.dto.ResponseDto.OrderResponseDto;
import com.ThreeK_Project.api_server.domain.order.entity.Order;
import com.ThreeK_Project.api_server.domain.order.entity.OrderProduct;
import com.ThreeK_Project.api_server.domain.order.enums.OrderStatus;
import com.ThreeK_Project.api_server.domain.order.repository.OrderProductRepository;
import com.ThreeK_Project.api_server.domain.order.repository.OrderRepository;
import com.ThreeK_Project.api_server.domain.product.entity.Product;
import com.ThreeK_Project.api_server.domain.product.service.ProductService;
import com.ThreeK_Project.api_server.domain.restaurant.entity.Restaurant;
import com.ThreeK_Project.api_server.domain.user.entity.User;
import com.ThreeK_Project.api_server.global.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductService productService;

    // 사용자 -> 주문 생성
    @Transactional
    public String createOrder(OrderRequestDto requestDto, Restaurant restaurant) {
        Order savedOrder = saveOrder(requestDto, restaurant);
        requestDto.getProductList()
                .forEach(productData -> {saveOrderProduct(productData, savedOrder);});

        return "주문 생성 성공";
    }

    // 사용자 -> 주문 취소
    // createBy -> fetchJoin으로 한번에 가져오기
    // 결제 취소도 불러오기
    @Transactional
    public void cancelOrder(UUID orderId, String username) {
        LocalDateTime now = LocalDateTime.now();
        Order order = findOrderById(orderId);

        if(!order.getCreatedBy().getUsername().equals(username))
            throw new ApplicationException("Invalid user");

        if(order.getOrderStatus() != OrderStatus.WAIT)
            throw new ApplicationException("Cannot cancel");

        if(Duration.between(order.getCreatedAt(), now).toMinutes() >= 5)
            throw new ApplicationException("Cancel Timeout");

        order.changeStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }

    // 가게 주인 -> 주문 상태 변경
    @Transactional
    public void updateOrderStatus(UUID orderId, OrderStatus newOrderStatus) {
        if(newOrderStatus == OrderStatus.WAIT)
            throw new ApplicationException("Invalid order status");

        Order order = findOrderById(orderId);

        OrderStatus oldOrderStatus = order.getOrderStatus();
        if(oldOrderStatus == OrderStatus.WAIT && newOrderStatus == OrderStatus.CANCELED)
            order.changeStatus(OrderStatus.CANCELED);
        else if(oldOrderStatus == OrderStatus.WAIT && newOrderStatus == OrderStatus.RECEIPT)
            order.changeStatus(OrderStatus.RECEIPT);
        else if(oldOrderStatus == OrderStatus.RECEIPT && newOrderStatus == OrderStatus.COMPLETE)
            order.changeStatus(OrderStatus.COMPLETE);
        else
            throw new ApplicationException("Invalid order status");
        orderRepository.save(order);
    }

    // 주문 조회
    public OrderResponseDto getOrder(UUID orderId) {
        Order order = findOrderByIdWithProductsAndPayment(orderId);
        return new OrderResponseDto(order);
    }

    // 사용자 주문 검색
    public Page<OrderResponseDto> searchUserOrders(String username, OrderSearchDTO searchDTO) {
        if(searchDTO.getUsername() == null)
            searchDTO.setUsername(username);
        else if(!searchDTO.getUsername().equals(username))
            throw new ApplicationException("Invalid user");

        return searchOrders(searchDTO);
    }

    // 가게 주인 주문 검색
    public Page<OrderResponseDto> searchRestaurantOrders(String restaurantName, OrderSearchDTO searchDTO) {
        if(searchDTO.getRestaurantName() == null)
            searchDTO.setRestaurantName(restaurantName);
        else if(!searchDTO.getRestaurantName().equals(restaurantName))
            throw new ApplicationException("Invalid restaurant");

        return searchOrders(searchDTO);
    }

    // 주문 검색
    public Page<OrderResponseDto> searchOrders(OrderSearchDTO searchDTO) {
        Sort sort = searchDTO.getAscending() ? Sort.by(Sort.Direction.DESC, "createdAt"). ascending()
                : Sort.by(Sort.Direction.ASC, "createdAt").descending();

        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);

        return orderRepository.searchOrders(pageable, searchDTO).map(OrderResponseDto::new);
    }

    // 주문 삭제
    @Transactional
    public void deleteOrder(UUID orderId, User user) {
        Order order = findOrderById(orderId);
        order.deleteBy(user);
        orderRepository.save(order);
    }

    @Transactional
    public Order saveOrder(OrderRequestDto requestDto, Restaurant restaurant) {
        Order order = Order.createOrder(
                requestDto.getOrderType(), OrderStatus.PAYMENT_WAIT, requestDto.getOrderAmount(),
                requestDto.getDeliveryAddress(), requestDto.getRequestDetails(), restaurant
        );
        return orderRepository.save(order);
    }

    @Transactional
    public OrderProduct saveOrderProduct(ProductRequestData productData, Order order) {
        Product product = productService.getProductById(productData.getProductId());

        OrderProduct orderProduct = OrderProduct.createOrderProduct(
                productData.getQuantity(), productData.getProductAmount(), order, product
        );
        return orderProductRepository.save(orderProduct);
    }

    public Order findOrderById(UUID orderId){
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException("Order not found"));
    }

    public Order findOrderByIdWithProductsAndPayment(UUID orderId){
        return orderRepository.findByIdWithProductsAndPayment(orderId)
                .orElseThrow(() -> new ApplicationException("Order not found"));
    }
}