package interview.prep.unittests.repository;

import interview.prep.unittests.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findByUserId(Long userId);
    List<Order> findAll();
    Order update(Order order);
    void deleteById(Long id);
}
