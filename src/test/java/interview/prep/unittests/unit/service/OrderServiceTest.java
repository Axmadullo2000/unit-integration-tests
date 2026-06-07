package interview.prep.unittests.unit.service;

import interview.prep.unittests.dto.request.CreateOrderRequest;
import interview.prep.unittests.dto.request.UpdateOrderRequest;
import interview.prep.unittests.dto.request.UpdateOrderStatusRequest;
import interview.prep.unittests.dto.response.OrderItemResponse;
import interview.prep.unittests.dto.response.OrderResponse;
import interview.prep.unittests.entity.Order;
import interview.prep.unittests.entity.OrderItem;
import interview.prep.unittests.entity.OrderStatus;
import interview.prep.unittests.exception.ResourceNotFoundException;
import interview.prep.unittests.exception.UnauthorizedException;
import interview.prep.unittests.helper.OrderItemTestDataHelper;
import interview.prep.unittests.helper.OrderTestDataHelper;
import interview.prep.unittests.mapper.OrderItemMapper;
import interview.prep.unittests.mapper.OrderMapper;
import interview.prep.unittests.repository.OrderItemRepository;
import interview.prep.unittests.repository.OrderRepository;
import interview.prep.unittests.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private OrderItem testOrderItem;
    private OrderResponse testOrderResponse;
    private CreateOrderRequest createOrderRequest;

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    @BeforeEach
    void setUp() {
        testOrder = OrderTestDataHelper.createOrder(1L, USER_ID);
        testOrderItem = OrderItemTestDataHelper.createOrderItem(1L, 1L);
        testOrderResponse = OrderTestDataHelper.createOrderResponse(1L, USER_ID);
        createOrderRequest = OrderTestDataHelper.createCreateOrderRequest();
    }

    @Nested
    @DisplayName("createOrder")
    class CreateOrder {

        @Test
        @DisplayName("should create order with items successfully")
        void shouldCreateOrderWithItemsSuccessfully() {
            // given
            List<OrderItem> items = List.of(testOrderItem);

            when(orderMapper.toEntity(any(CreateOrderRequest.class), eq(USER_ID))).thenReturn(testOrder);
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
            when(orderItemMapper.toEntityList(any(), eq(1L))).thenReturn(items);
            when(orderItemRepository.saveAll(any())).thenReturn(items);
            when(orderMapper.toResponse(any(Order.class), any())).thenReturn(testOrderResponse);

            // when
            OrderResponse result = orderService.createOrder(USER_ID, createOrderRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(USER_ID);
            assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);

            verify(orderRepository).save(any(Order.class));
            verify(orderItemRepository).saveAll(any());
        }

        @Test
        @DisplayName("should calculate total price from items")
        void shouldCalculateTotalPriceFromItems() {
            // given
            CreateOrderRequest requestWith2Items = OrderTestDataHelper.createCreateOrderRequest(2);
            List<OrderItem> items = OrderItemTestDataHelper.createOrderItemList(1L, 2);

            when(orderMapper.toEntity(any(CreateOrderRequest.class), eq(USER_ID))).thenReturn(testOrder);
            when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
            when(orderItemMapper.toEntityList(any(), eq(1L))).thenReturn(items);
            when(orderItemRepository.saveAll(any())).thenReturn(items);
            when(orderMapper.toResponse(any(Order.class), any())).thenReturn(testOrderResponse);

            // when
            OrderResponse result = orderService.createOrder(USER_ID, requestWith2Items);

            // then
            assertThat(result).isNotNull();
            verify(orderMapper).toEntity(any(CreateOrderRequest.class), eq(USER_ID));
        }
    }

    @Nested
    @DisplayName("getOrderById")
    class GetOrderById {

        @Test
        @DisplayName("should return order when found and user owns it")
        void shouldReturnOrderWhenFoundAndOwned() {
            // given
            List<OrderItem> items = List.of(testOrderItem);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderItemRepository.findByOrderId(1L)).thenReturn(items);
            when(orderMapper.toResponse(testOrder, items)).thenReturn(testOrderResponse);

            // when
            OrderResponse result = orderService.getOrderById(1L, USER_ID);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);

            verify(orderRepository).findById(1L);
            verify(orderItemRepository).findByOrderId(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when order not found")
        void shouldThrowWhenOrderNotFound() {
            // given
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> orderService.getOrderById(999L, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");

            verify(orderRepository).findById(999L);
        }

        @Test
        @DisplayName("should throw UnauthorizedException when user doesn't own order")
        void shouldThrowWhenUserDoesNotOwnOrder() {
            // given
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            // when/then
            assertThatThrownBy(() -> orderService.getOrderById(1L, OTHER_USER_ID))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("permission");

            verify(orderRepository).findById(1L);
            verify(orderItemRepository, never()).findByOrderId(anyLong());
        }
    }

    @Nested
    @DisplayName("getOrdersByUserId")
    class GetOrdersByUserId {

        @Test
        @DisplayName("should return all orders for user")
        void shouldReturnAllOrdersForUser() {
            // given
            Order order2 = OrderTestDataHelper.createOrder(2L, USER_ID);
            List<OrderItem> items1 = List.of(testOrderItem);
            List<OrderItem> items2 = List.of(OrderItemTestDataHelper.createOrderItem(2L, 2L));

            when(orderRepository.findByUserId(USER_ID)).thenReturn(List.of(testOrder, order2));
            when(orderItemRepository.findByOrderId(1L)).thenReturn(items1);
            when(orderItemRepository.findByOrderId(2L)).thenReturn(items2);
            when(orderMapper.toResponse(eq(testOrder), any())).thenReturn(testOrderResponse);
            when(orderMapper.toResponse(eq(order2), any())).thenReturn(OrderTestDataHelper.createOrderResponse(2L, USER_ID));

            // when
            List<OrderResponse> result = orderService.getOrdersByUserId(USER_ID);

            // then
            assertThat(result).hasSize(2);

            verify(orderRepository).findByUserId(USER_ID);
        }

        @Test
        @DisplayName("should return empty list when user has no orders")
        void shouldReturnEmptyListWhenNoOrders() {
            // given
            when(orderRepository.findByUserId(USER_ID)).thenReturn(Collections.emptyList());

            // when
            List<OrderResponse> result = orderService.getOrdersByUserId(USER_ID);

            // then
            assertThat(result).isEmpty();

            verify(orderRepository).findByUserId(USER_ID);
        }
    }

    @Nested
    @DisplayName("updateOrder")
    class UpdateOrder {

        @Test
        @DisplayName("should update order items successfully")
        void shouldUpdateOrderItemsSuccessfully() {
            // given
            UpdateOrderRequest updateRequest = OrderTestDataHelper.createUpdateOrderRequest(2);
            List<OrderItem> newItems = OrderItemTestDataHelper.createOrderItemList(1L, 2);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            doNothing().when(orderItemRepository).deleteByOrderId(1L);
            when(orderItemMapper.toEntityList(any(), eq(1L))).thenReturn(newItems);
            when(orderItemRepository.saveAll(any())).thenReturn(newItems);
            doNothing().when(orderMapper).updateEntity(any(Order.class), any(BigDecimal.class));
            when(orderRepository.update(any(Order.class))).thenReturn(testOrder);
            when(orderMapper.toResponse(any(Order.class), any())).thenReturn(testOrderResponse);

            // when
            OrderResponse result = orderService.updateOrder(1L, USER_ID, updateRequest);

            // then
            assertThat(result).isNotNull();

            verify(orderItemRepository).deleteByOrderId(1L);
            verify(orderItemRepository).saveAll(any());
            verify(orderRepository).update(any(Order.class));
        }

        @Test
        @DisplayName("should recalculate total price on update")
        void shouldRecalculateTotalPriceOnUpdate() {
            // given
            UpdateOrderRequest updateRequest = OrderTestDataHelper.createUpdateOrderRequest(3);
            List<OrderItem> newItems = OrderItemTestDataHelper.createOrderItemList(1L, 3);
            BigDecimal expectedTotal = new BigDecimal("450.00");

            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            doNothing().when(orderItemRepository).deleteByOrderId(1L);
            when(orderItemMapper.toEntityList(any(), eq(1L))).thenReturn(newItems);
            when(orderItemRepository.saveAll(any())).thenReturn(newItems);
            doNothing().when(orderMapper).updateEntity(any(Order.class), any(BigDecimal.class));
            when(orderRepository.update(any(Order.class))).thenReturn(testOrder);
            when(orderMapper.toResponse(any(Order.class), any())).thenReturn(testOrderResponse);

            // when
            orderService.updateOrder(1L, USER_ID, updateRequest);

            // then
            verify(orderMapper).updateEntity(eq(testOrder), eq(expectedTotal));
        }

        @Test
        @DisplayName("should throw UnauthorizedException when user doesn't own order")
        void shouldThrowWhenUserDoesNotOwnOrder() {
            // given
            UpdateOrderRequest updateRequest = OrderTestDataHelper.createUpdateOrderRequest();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            // when/then
            assertThatThrownBy(() -> orderService.updateOrder(1L, OTHER_USER_ID, updateRequest))
                    .isInstanceOf(UnauthorizedException.class);

            verify(orderItemRepository, never()).deleteByOrderId(anyLong());
        }
    }

    @Nested
    @DisplayName("updateOrderStatus")
    class UpdateOrderStatus {

        @Test
        @DisplayName("should update order status successfully")
        void shouldUpdateOrderStatusSuccessfully() {
            // given
            UpdateOrderStatusRequest statusRequest = OrderTestDataHelper.createUpdateOrderStatusRequest(OrderStatus.CONFIRMED);
            List<OrderItem> items = List.of(testOrderItem);

            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.update(any(Order.class))).thenReturn(testOrder);
            when(orderItemRepository.findByOrderId(1L)).thenReturn(items);
            when(orderMapper.toResponse(any(Order.class), any())).thenReturn(testOrderResponse);

            // when
            OrderResponse result = orderService.updateOrderStatus(1L, USER_ID, statusRequest);

            // then
            assertThat(result).isNotNull();

            verify(orderRepository).update(any(Order.class));
        }

        @Test
        @DisplayName("should throw UnauthorizedException when user doesn't own order")
        void shouldThrowWhenUserDoesNotOwnOrder() {
            // given
            UpdateOrderStatusRequest statusRequest = OrderTestDataHelper.createUpdateOrderStatusRequest(OrderStatus.CONFIRMED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            // when/then
            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, OTHER_USER_ID, statusRequest))
                    .isInstanceOf(UnauthorizedException.class);

            verify(orderRepository, never()).update(any(Order.class));
        }

        @ParameterizedTest(name = "should update order status to {0}")
        @EnumSource(OrderStatus.class)
        @DisplayName("should update order to all valid statuses")
        void shouldUpdateToAllValidStatuses(OrderStatus targetStatus) {
            // given
            UpdateOrderStatusRequest statusRequest = OrderTestDataHelper.createUpdateOrderStatusRequest(targetStatus);
            List<OrderItem> items = List.of(testOrderItem);

            OrderResponse expectedResponse = OrderResponse.builder()
                    .id(1L)
                    .userId(USER_ID)
                    .status(targetStatus)
                    .totalPrice(testOrder.getTotalPrice())
                    .items(List.of(OrderItemTestDataHelper.createOrderItemResponse()))
                    .createdAt(testOrder.getCreatedAt())
                    .updatedAt(testOrder.getUpdatedAt())
                    .build();

            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(orderRepository.update(any(Order.class))).thenReturn(testOrder);
            when(orderItemRepository.findByOrderId(1L)).thenReturn(items);
            when(orderMapper.toResponse(any(Order.class), any())).thenReturn(expectedResponse);

            // when
            OrderResponse result = orderService.updateOrderStatus(1L, USER_ID, statusRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(targetStatus);

            verify(orderRepository).findById(1L);
            verify(orderRepository).update(any(Order.class));
        }
    }

    @Nested
    @DisplayName("deleteOrder")
    class DeleteOrder {

        @Test
        @DisplayName("should delete order successfully")
        void shouldDeleteOrderSuccessfully() {
            // given
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            doNothing().when(orderRepository).deleteById(1L);

            // when
            orderService.deleteOrder(1L, USER_ID);

            // then
            verify(orderRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when order not found")
        void shouldThrowWhenOrderNotFound() {
            // given
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> orderService.deleteOrder(999L, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(orderRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("should throw UnauthorizedException when user doesn't own order")
        void shouldThrowWhenUserDoesNotOwnOrder() {
            // given
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            // when/then
            assertThatThrownBy(() -> orderService.deleteOrder(1L, OTHER_USER_ID))
                    .isInstanceOf(UnauthorizedException.class);

            verify(orderRepository, never()).deleteById(anyLong());
        }
    }
}
