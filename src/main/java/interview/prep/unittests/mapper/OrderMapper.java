package interview.prep.unittests.mapper;

import interview.prep.unittests.dto.request.CreateOrderRequest;
import interview.prep.unittests.dto.response.OrderResponse;
import interview.prep.unittests.entity.Order;
import interview.prep.unittests.entity.OrderItem;
import interview.prep.unittests.entity.OrderStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderMapper {

    private final OrderItemMapper orderItemMapper;

    public OrderMapper(OrderItemMapper orderItemMapper) {
        this.orderItemMapper = orderItemMapper;
    }

    public Order toEntity(CreateOrderRequest request, Long userId) {
        BigDecimal totalPrice = request.getItems().stream()
                .map(item -> item.getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime now = LocalDateTime.now();
        return Order.builder()
                .userId(userId)
                .status(OrderStatus.PENDING)
                .totalPrice(totalPrice)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public OrderResponse toResponse(Order order, List<OrderItem> items) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .items(orderItemMapper.toResponseList(items))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public void updateEntity(Order order, BigDecimal newTotalPrice) {
        order.setTotalPrice(newTotalPrice);
        order.setUpdatedAt(LocalDateTime.now());
    }
}
