package interview.prep.unittests.repository;

import interview.prep.unittests.entity.OrderItem;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository {
    OrderItem save(OrderItem orderItem);
    List<OrderItem> saveAll(List<OrderItem> orderItems);
    Optional<OrderItem> findById(Long id);
    List<OrderItem> findByOrderId(Long orderId);
    void deleteByOrderId(Long orderId);
}
