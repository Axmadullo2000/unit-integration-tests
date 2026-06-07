package interview.prep.unittests.integration;

import interview.prep.unittests.dto.request.*;
import interview.prep.unittests.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Factory class for creating test data.
 * Use this to create consistent test objects across tests.
 */
public class TestDataFactory {

    // ==================== Request DTOs ====================

    public static RegisterRequest createRegisterRequest() {
        return RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    public static RegisterRequest createRegisterRequest(String email) {
        return RegisterRequest.builder()
                .email(email)
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    public static LoginRequest createLoginRequest() {
        return LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();
    }

    public static LoginRequest createLoginRequest(String email, String password) {
        return LoginRequest.builder()
                .email(email)
                .password(password)
                .build();
    }

    public static CreateUserRequest createUserRequest() {
        return CreateUserRequest.builder()
                .email("newuser@example.com")
                .password("password123")
                .firstName("Jane")
                .lastName("Smith")
                .build();
    }

    public static UpdateUserRequest createUpdateUserRequest() {
        return UpdateUserRequest.builder()
                .firstName("UpdatedFirst")
                .lastName("UpdatedLast")
                .build();
    }

    public static CreateOrderItemRequest createOrderItemRequest() {
        return CreateOrderItemRequest.builder()
                .eventName("Rock Concert")
                .eventDate(LocalDateTime.now().plusDays(30))
                .venue("Madison Square Garden")
                .seatNumber("A-101")
                .price(new BigDecimal("150.00"))
                .build();
    }

    public static CreateOrderItemRequest createOrderItemRequest(String seatNumber, BigDecimal price) {
        return CreateOrderItemRequest.builder()
                .eventName("Rock Concert")
                .eventDate(LocalDateTime.now().plusDays(30))
                .venue("Madison Square Garden")
                .seatNumber(seatNumber)
                .price(price)
                .build();
    }

    public static CreateOrderRequest createOrderRequest() {
        return CreateOrderRequest.builder()
                .items(List.of(createOrderItemRequest()))
                .build();
    }

    public static CreateOrderRequest createOrderRequest(List<CreateOrderItemRequest> items) {
        return CreateOrderRequest.builder()
                .items(items)
                .build();
    }

    public static UpdateOrderStatusRequest createUpdateOrderStatusRequest(OrderStatus status) {
        return UpdateOrderStatusRequest.builder()
                .status(status)
                .build();
    }

    // ==================== Entities ====================

    public static User createUser() {
        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static User createUser(Long id, String email) {
        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .id(id)
                .email(email)
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static Order createOrder(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return Order.builder()
                .id(1L)
                .userId(userId)
                .status(OrderStatus.PENDING)
                .totalPrice(new BigDecimal("150.00"))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static OrderItem createOrderItem(Long orderId) {
        return OrderItem.builder()
                .id(1L)
                .orderId(orderId)
                .eventName("Rock Concert")
                .eventDate(LocalDateTime.now().plusDays(30))
                .venue("Madison Square Garden")
                .seatNumber("A-101")
                .price(new BigDecimal("150.00"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static RefreshToken createRefreshToken(Long userId) {
        return RefreshToken.builder()
                .id(1L)
                .userId(userId)
                .token("valid-refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
    }
}
