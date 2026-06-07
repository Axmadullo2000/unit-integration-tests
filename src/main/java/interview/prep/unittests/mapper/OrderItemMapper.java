package interview.prep.unittests.mapper;

import interview.prep.unittests.dto.request.CreateOrderItemRequest;
import interview.prep.unittests.dto.response.OrderItemResponse;
import interview.prep.unittests.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderItemMapper {

    public OrderItem toEntity(CreateOrderItemRequest request, Long orderId) {
        return OrderItem.builder()
                .orderId(orderId)
                .eventName(request.getEventName())
                .eventDate(request.getEventDate())
                .venue(request.getVenue())
                .seatNumber(request.getSeatNumber())
                .price(request.getPrice())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public OrderItemResponse toResponse(OrderItem item) {
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

    public List<OrderItem> toEntityList(List<CreateOrderItemRequest> requests, Long orderId) {
        return requests.stream()
                .map(request -> toEntity(request, orderId))
                .collect(Collectors.toList());
    }

    public List<OrderItemResponse> toResponseList(List<OrderItem> items) {
        return items.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
