package interview.prep.unittests.helper;

import interview.prep.unittests.dto.request.CreateOrderItemRequest;
import interview.prep.unittests.dto.response.OrderItemResponse;
import interview.prep.unittests.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OrderItemTestDataHelper {

    public static final String DEFAULT_EVENT_NAME = "Rock Concert";
    public static final String DEFAULT_VENUE = "Madison Square Garden";
    public static final String DEFAULT_SEAT_NUMBER = "A-101";
    public static final BigDecimal DEFAULT_PRICE = new BigDecimal("150.00");

    // ==================== Entity ====================

    public static OrderItem createOrderItem() {
        return createOrderItem(1L, 1L);
    }

    public static OrderItem createOrderItem(Long id) {
        return createOrderItem(id, 1L);
    }

    public static OrderItem createOrderItem(Long id, Long orderId) {
        return OrderItem.builder()
                .id(id)
                .orderId(orderId)
                .eventName(DEFAULT_EVENT_NAME)
                .eventDate(LocalDateTime.now().plusDays(30))
                .venue(DEFAULT_VENUE)
                .seatNumber(DEFAULT_SEAT_NUMBER)
                .price(DEFAULT_PRICE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static OrderItem createOrderItem(Long id, Long orderId, String seatNumber, BigDecimal price) {
        return OrderItem.builder()
                .id(id)
                .orderId(orderId)
                .eventName(DEFAULT_EVENT_NAME)
                .eventDate(LocalDateTime.now().plusDays(30))
                .venue(DEFAULT_VENUE)
                .seatNumber(seatNumber)
                .price(price)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static OrderItem.OrderItemBuilder createOrderItemBuilder() {
        return OrderItem.builder()
                .id(1L)
                .orderId(1L)
                .eventName(DEFAULT_EVENT_NAME)
                .eventDate(LocalDateTime.now().plusDays(30))
                .venue(DEFAULT_VENUE)
                .seatNumber(DEFAULT_SEAT_NUMBER)
                .price(DEFAULT_PRICE)
                .createdAt(LocalDateTime.now());
    }

    public static List<OrderItem> createOrderItemList(Long orderId, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> createOrderItem((long) (i + 1), orderId, "A-" + (101 + i), DEFAULT_PRICE))
                .toList();
    }

    // ==================== Request DTOs ====================

    public static CreateOrderItemRequest createCreateOrderItemRequest() {
        return CreateOrderItemRequest.builder()
                .eventName(DEFAULT_EVENT_NAME)
                .eventDate(LocalDateTime.now().plusDays(30))
                .venue(DEFAULT_VENUE)
                .seatNumber(DEFAULT_SEAT_NUMBER)
                .price(DEFAULT_PRICE)
                .build();
    }

    public static CreateOrderItemRequest createCreateOrderItemRequest(String seatNumber, BigDecimal price) {
        return CreateOrderItemRequest.builder()
                .eventName(DEFAULT_EVENT_NAME)
                .eventDate(LocalDateTime.now().plusDays(30))
                .venue(DEFAULT_VENUE)
                .seatNumber(seatNumber)
                .price(price)
                .build();
    }

    public static List<CreateOrderItemRequest> createCreateOrderItemRequestList(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> createCreateOrderItemRequest("A-" + (101 + i), DEFAULT_PRICE))
                .toList();
    }

    // ==================== Response DTOs ====================

    public static OrderItemResponse createOrderItemResponse() {
        return createOrderItemResponse(1L);
    }

    public static OrderItemResponse createOrderItemResponse(Long id) {
        return OrderItemResponse.builder()
                .id(id)
                .eventName(DEFAULT_EVENT_NAME)
                .eventDate(LocalDateTime.now().plusDays(30))
                .venue(DEFAULT_VENUE)
                .seatNumber(DEFAULT_SEAT_NUMBER)
                .price(DEFAULT_PRICE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static OrderItemResponse createOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .eventName(item.getEventName())
                .eventDate(item.getEventDate())
                .venue(item.getVenue())
                .seatNumber(item.getSeatNumber())
                .price(item.getPrice())
                .createdAt(item.getCreatedAt())
                .build();
    }

    public static List<OrderItemResponse> createOrderItemResponseList(List<OrderItem> items) {
        return items.stream()
                .map(OrderItemTestDataHelper::createOrderItemResponse)
                .collect(Collectors.toList());
    }
}
