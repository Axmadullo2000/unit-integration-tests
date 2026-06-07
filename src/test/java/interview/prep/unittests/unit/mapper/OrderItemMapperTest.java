package interview.prep.unittests.unit.mapper;

import interview.prep.unittests.dto.request.CreateOrderItemRequest;
import interview.prep.unittests.dto.response.OrderItemResponse;
import interview.prep.unittests.entity.OrderItem;
import interview.prep.unittests.helper.OrderItemTestDataHelper;
import interview.prep.unittests.mapper.OrderItemMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemMapperTest {

    private OrderItemMapper orderItemMapper;

    @BeforeEach
    void setUp() {
        orderItemMapper = new OrderItemMapper();
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("should map all fields correctly")
        void shouldMapAllFieldsCorrectly() {
            // given
            CreateOrderItemRequest request = OrderItemTestDataHelper.createCreateOrderItemRequest();
            Long orderId = 1L;

            // when
            OrderItem result = orderItemMapper.toEntity(request, orderId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getEventName()).isEqualTo(request.getEventName());
            assertThat(result.getEventDate()).isEqualTo(request.getEventDate());
            assertThat(result.getVenue()).isEqualTo(request.getVenue());
            assertThat(result.getSeatNumber()).isEqualTo(request.getSeatNumber());
            assertThat(result.getPrice()).isEqualTo(request.getPrice());
        }

        @Test
        @DisplayName("should set orderId")
        void shouldSetOrderId() {
            // given
            CreateOrderItemRequest request = OrderItemTestDataHelper.createCreateOrderItemRequest();
            Long orderId = 42L;

            // when
            OrderItem result = orderItemMapper.toEntity(request, orderId);

            // then
            assertThat(result.getOrderId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("should set createdAt timestamp")
        void shouldSetCreatedAt() {
            // given
            CreateOrderItemRequest request = OrderItemTestDataHelper.createCreateOrderItemRequest();
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            // when
            OrderItem result = orderItemMapper.toEntity(request, 1L);

            // then
            assertThat(result.getCreatedAt()).isAfter(before);
        }
    }

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("should map all fields correctly")
        void shouldMapAllFieldsCorrectly() {
            // given
            OrderItem item = OrderItemTestDataHelper.createOrderItem();

            // when
            OrderItemResponse result = orderItemMapper.toResponse(item);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(item.getId());
            assertThat(result.getEventName()).isEqualTo(item.getEventName());
            assertThat(result.getEventDate()).isEqualTo(item.getEventDate());
            assertThat(result.getVenue()).isEqualTo(item.getVenue());
            assertThat(result.getSeatNumber()).isEqualTo(item.getSeatNumber());
            assertThat(result.getPrice()).isEqualTo(item.getPrice());
            assertThat(result.getCreatedAt()).isEqualTo(item.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("toEntityList")
    class ToEntityList {

        @Test
        @DisplayName("should map all items in list")
        void shouldMapAllItems() {
            // given
            List<CreateOrderItemRequest> requests = OrderItemTestDataHelper.createCreateOrderItemRequestList(3);
            Long orderId = 1L;

            // when
            List<OrderItem> result = orderItemMapper.toEntityList(requests, orderId);

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getSeatNumber()).isEqualTo("A-101");
            assertThat(result.get(1).getSeatNumber()).isEqualTo("A-102");
            assertThat(result.get(2).getSeatNumber()).isEqualTo("A-103");
        }

        @Test
        @DisplayName("should set same orderId for all items")
        void shouldSetSameOrderId() {
            // given
            List<CreateOrderItemRequest> requests = OrderItemTestDataHelper.createCreateOrderItemRequestList(5);
            Long orderId = 99L;

            // when
            List<OrderItem> result = orderItemMapper.toEntityList(requests, orderId);

            // then
            assertThat(result).allMatch(item -> item.getOrderId().equals(99L));
        }
    }

    @Nested
    @DisplayName("toResponseList")
    class ToResponseList {

        @Test
        @DisplayName("should map all items in list")
        void shouldMapAllItems() {
            // given
            List<OrderItem> items = OrderItemTestDataHelper.createOrderItemList(1L, 4);

            // when
            List<OrderItemResponse> result = orderItemMapper.toResponseList(items);

            // then
            assertThat(result).hasSize(4);
            for (int i = 0; i < items.size(); i++) {
                assertThat(result.get(i).getId()).isEqualTo(items.get(i).getId());
                assertThat(result.get(i).getEventName()).isEqualTo(items.get(i).getEventName());
                assertThat(result.get(i).getPrice()).isEqualTo(items.get(i).getPrice());
            }
        }
    }
}
