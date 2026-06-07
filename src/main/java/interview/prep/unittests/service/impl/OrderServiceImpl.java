package interview.prep.unittests.service.impl;

import interview.prep.unittests.dto.request.CreateOrderRequest;
import interview.prep.unittests.dto.request.UpdateOrderRequest;
import interview.prep.unittests.dto.request.UpdateOrderStatusRequest;
import interview.prep.unittests.dto.response.OrderResponse;
import interview.prep.unittests.entity.Order;
import interview.prep.unittests.entity.OrderItem;
import interview.prep.unittests.exception.ResourceNotFoundException;
import interview.prep.unittests.exception.UnauthorizedException;
import interview.prep.unittests.mapper.OrderItemMapper;
import interview.prep.unittests.mapper.OrderMapper;
import interview.prep.unittests.repository.OrderItemRepository;
import interview.prep.unittests.repository.OrderRepository;
import interview.prep.unittests.service.OrderService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                           OrderMapper orderMapper, OrderItemMapper orderItemMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        Order order = orderMapper.toEntity(request, userId);
        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = orderItemMapper.toEntityList(request.getItems(), savedOrder.getId());
        List<OrderItem> savedItems = orderItemRepository.saveAll(orderItems);

        return orderMapper.toResponse(savedOrder, savedItems);
    }

    @Override
    public OrderResponse getOrderById(Long id, Long userId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        validateOrderOwnership(order, userId);

        List<OrderItem> items = orderItemRepository.findByOrderId(id);
        return orderMapper.toResponse(order, items);
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    return orderMapper.toResponse(order, items);
                })
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrder(Long id, Long userId, UpdateOrderRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        validateOrderOwnership(order, userId);

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            orderItemRepository.deleteByOrderId(id);

            List<OrderItem> newItems = orderItemMapper.toEntityList(request.getItems(), id);
            List<OrderItem> savedItems = orderItemRepository.saveAll(newItems);

            BigDecimal newTotalPrice = savedItems.stream()
                    .map(OrderItem::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            orderMapper.updateEntity(order, newTotalPrice);
            Order updatedOrder = orderRepository.update(order);

            return orderMapper.toResponse(updatedOrder, savedItems);
        }

        List<OrderItem> existingItems = orderItemRepository.findByOrderId(id);
        return orderMapper.toResponse(order, existingItems);
    }

    @Override
    public OrderResponse updateOrderStatus(Long id, Long userId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        validateOrderOwnership(order, userId);

        order.setStatus(request.getStatus());
        order.setUpdatedAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.update(order);

        List<OrderItem> items = orderItemRepository.findByOrderId(id);
        return orderMapper.toResponse(updatedOrder, items);
    }

    @Override
    public void deleteOrder(Long id, Long userId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        validateOrderOwnership(order, userId);

        orderRepository.deleteById(id);
    }

    private void validateOrderOwnership(Order order, Long userId) {
        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to access this order");
        }
    }
}
