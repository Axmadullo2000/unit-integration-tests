package interview.prep.unittests.helper;

import interview.prep.unittests.dto.request.CreateOrderRequest;
import interview.prep.unittests.dto.request.UpdateOrderRequest;
import interview.prep.unittests.dto.request.UpdateOrderStatusRequest;
import interview.prep.unittests.dto.response.OrderResponse;
import interview.prep.unittests.entity.Order;
import interview.prep.unittests.entity.OrderItem;
import interview.prep.unittests.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderTestDataHelper {

    public static final Long DEFAULT_USER_ID = 1L;
    public static final BigDecimal DEFAULT_TOTAL_PRICE = new BigDecimal("150.00");

    // ==================== Entity ====================

    public static Order createOrder() {
        return createOrder(1L, DEFAULT_USER_ID);
    }

    public static Order createOrder(Long id) {
        return createOrder(id, DEFAULT_USER_ID);
    }

    public static Order createOrder(Long id, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return Order.builder()
                .id(id)
                .userId(userId)
                .status(OrderStatus.PENDING)
                .totalPrice(DEFAULT_TOTAL_PRICE)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static Order createOrder(Long id, Long userId, OrderStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return Order.builder()
                .id(id)
                .userId(userId)
                .status(status)
                .totalPrice(DEFAULT_TOTAL_PRICE)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static Order.OrderBuilder createOrderBuilder() {
        LocalDateTime now = LocalDateTime.now();
        return Order.builder()
                .id(1L)
                .userId(DEFAULT_USER_ID)
                .status(OrderStatus.PENDING)
                .totalPrice(DEFAULT_TOTAL_PRICE)
                .createdAt(now)
                .updatedAt(now);
    }

    // ==================== Request DTOs ====================

    public static CreateOrderRequest createCreateOrderRequest() {
        return CreateOrderRequest.builder()
                .items(List.of(OrderItemTestDataHelper.createCreateOrderItemRequest()))
                .build();
    }

    public static CreateOrderRequest createCreateOrderRequest(int itemCount) {
        return CreateOrderRequest.builder()
                .items(OrderItemTestDataHelper.createCreateOrderItemRequestList(itemCount))
                .build();
    }

    public static UpdateOrderRequest createUpdateOrderRequest() {
        return UpdateOrderRequest.builder()
                .items(List.of(OrderItemTestDataHelper.createCreateOrderItemRequest()))
                .build();
    }

    public static UpdateOrderRequest createUpdateOrderRequest(int itemCount) {
        return UpdateOrderRequest.builder()
                .items(OrderItemTestDataHelper.createCreateOrderItemRequestList(itemCount))
                .build();
    }

    public static UpdateOrderStatusRequest createUpdateOrderStatusRequest(OrderStatus status) {
        return UpdateOrderStatusRequest.builder()
                .status(status)
                .build();
    }

    // ==================== Response DTOs ====================

    public static OrderResponse createOrderResponse() {
        return createOrderResponse(1L, DEFAULT_USER_ID);
    }

    public static OrderResponse createOrderResponse(Long id, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return OrderResponse.builder()
                .id(id)
                .userId(userId)
                .status(OrderStatus.PENDING)
                .totalPrice(DEFAULT_TOTAL_PRICE)
                .items(List.of(OrderItemTestDataHelper.createOrderItemResponse()))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static OrderResponse createOrderResponse(Order order, List<OrderItem> items) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .items(OrderItemTestDataHelper.createOrderItemResponseList(items))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
