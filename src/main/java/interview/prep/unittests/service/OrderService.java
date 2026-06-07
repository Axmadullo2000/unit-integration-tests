package interview.prep.unittests.service;

import interview.prep.unittests.dto.request.CreateOrderRequest;
import interview.prep.unittests.dto.request.UpdateOrderRequest;
import interview.prep.unittests.dto.request.UpdateOrderStatusRequest;
import interview.prep.unittests.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(Long userId, CreateOrderRequest request);
    OrderResponse getOrderById(Long id, Long userId);
    List<OrderResponse> getOrdersByUserId(Long userId);
    OrderResponse updateOrder(Long id, Long userId, UpdateOrderRequest request);
    OrderResponse updateOrderStatus(Long id, Long userId, UpdateOrderStatusRequest request);
    void deleteOrder(Long id, Long userId);
}
