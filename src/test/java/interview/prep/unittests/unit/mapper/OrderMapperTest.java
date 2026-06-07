package interview.prep.unittests.unit.mapper;

import interview.prep.unittests.dto.request.CreateOrderItemRequest;
import interview.prep.unittests.dto.request.CreateOrderRequest;
import interview.prep.unittests.dto.response.OrderItemResponse;
import interview.prep.unittests.dto.response.OrderResponse;
import interview.prep.unittests.entity.Order;
import interview.prep.unittests.entity.OrderItem;
import interview.prep.unittests.entity.OrderStatus;
import interview.prep.unittests.helper.OrderItemTestDataHelper;
import interview.prep.unittests.helper.OrderTestDataHelper;
import interview.prep.unittests.mapper.OrderItemMapper;
import interview.prep.unittests.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderMapperTest {

    @Mock
    private OrderItemMapper orderItemMapper;

    private OrderMapper orderMapper;

    @BeforeEach
    void setUp() {
        orderMapper = new OrderMapper(orderItemMapper);
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("should map userId correctly")
        void shouldMapUserIdCorrectly() {
            // given
            CreateOrderRequest request = OrderTestDataHelper.createCreateOrderRequest();
            Long userId = 5L;

            // when
            Order result = orderMapper.toEntity(request, userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("should set status to PENDING")
        void shouldSetStatusToPending() {
            // given
            CreateOrderRequest request = OrderTestDataHelper.createCreateOrderRequest();

            // when
            Order result = orderMapper.toEntity(request, 1L);

            // then
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("should calculate total price from items")
        void shouldCalculateTotalPrice() {
            // given
            List<CreateOrderItemRequest> items = List.of(
                    OrderItemTestDataHelper.createCreateOrderItemRequest("A-1", new BigDecimal("100.00")),
                    OrderItemTestDataHelper.createCreateOrderItemRequest("A-2", new BigDecimal("150.00")),
                    OrderItemTestDataHelper.createCreateOrderItemRequest("A-3", new BigDecimal("50.00"))
            );
            CreateOrderRequest request = CreateOrderRequest.builder().items(items).build();

            // when
            Order result = orderMapper.toEntity(request, 1L);

            // then
            assertThat(result.getTotalPrice()).isEqualByComparingTo(new BigDecimal("300.00"));
        }

        @Test
        @DisplayName("should set timestamps")
        void shouldSetTimestamps() {
            // given
            CreateOrderRequest request = OrderTestDataHelper.createCreateOrderRequest();
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            // when
            Order result = orderMapper.toEntity(request, 1L);

            // then
            assertThat(result.getCreatedAt()).isAfter(before);
            assertThat(result.getUpdatedAt()).isAfter(before);
            assertThat(result.getCreatedAt()).isEqualTo(result.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("should map all order fields correctly")
        void shouldMapAllOrderFields() {
            // given
            Order order = OrderTestDataHelper.createOrder(1L, 2L);
            order.setStatus(OrderStatus.CONFIRMED);
            List<OrderItem> items = List.of(OrderItemTestDataHelper.createOrderItem());
            List<OrderItemResponse> itemResponses = List.of(OrderItemTestDataHelper.createOrderItemResponse());

            when(orderItemMapper.toResponseList(items)).thenReturn(itemResponses);

            // when
            OrderResponse result = orderMapper.toResponse(order, items);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(order.getId());
            assertThat(result.getUserId()).isEqualTo(order.getUserId());
            assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(result.getTotalPrice()).isEqualTo(order.getTotalPrice());
            assertThat(result.getCreatedAt()).isEqualTo(order.getCreatedAt());
            assertThat(result.getUpdatedAt()).isEqualTo(order.getUpdatedAt());
        }

        @Test
        @DisplayName("should include mapped items")
        void shouldIncludeMappedItems() {
            // given
            Order order = OrderTestDataHelper.createOrder();
            List<OrderItem> items = OrderItemTestDataHelper.createOrderItemList(1L, 3);
            List<OrderItemResponse> itemResponses = List.of(
                    OrderItemTestDataHelper.createOrderItemResponse(1L),
                    OrderItemTestDataHelper.createOrderItemResponse(2L),
                    OrderItemTestDataHelper.createOrderItemResponse(3L)
            );

            when(orderItemMapper.toResponseList(items)).thenReturn(itemResponses);

            // when
            OrderResponse result = orderMapper.toResponse(order, items);

            // then
            assertThat(result.getItems()).hasSize(3);
            assertThat(result.getItems()).isEqualTo(itemResponses);
        }
    }

    @Nested
    @DisplayName("updateEntity")
    class UpdateEntity {

        @Test
        @DisplayName("should update total price")
        void shouldUpdateTotalPrice() {
            // given
            Order order = OrderTestDataHelper.createOrder();
            BigDecimal originalPrice = order.getTotalPrice();
            BigDecimal newPrice = new BigDecimal("500.00");

            // when
            orderMapper.updateEntity(order, newPrice);

            // then
            assertThat(order.getTotalPrice()).isEqualByComparingTo(newPrice);
            assertThat(order.getTotalPrice()).isNotEqualByComparingTo(originalPrice);
        }

        @Test
        @DisplayName("should update updatedAt timestamp")
        void shouldUpdateTimestamp() {
            // given
            Order order = OrderTestDataHelper.createOrder();
            LocalDateTime originalUpdatedAt = order.getUpdatedAt();

            // small delay
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}

            // when
            orderMapper.updateEntity(order, new BigDecimal("200.00"));

            // then
            assertThat(order.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }
    }
}
