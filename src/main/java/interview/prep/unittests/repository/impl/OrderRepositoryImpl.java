package interview.prep.unittests.repository.impl;

import interview.prep.unittests.entity.Order;
import interview.prep.unittests.entity.OrderStatus;
import interview.prep.unittests.repository.OrderRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Order> rowMapper = (rs, rowNum) -> Order.builder()
            .id(rs.getLong("id"))
            .userId(rs.getLong("user_id"))
            .status(OrderStatus.valueOf(rs.getString("status")))
            .totalPrice(rs.getBigDecimal("total_price"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
            .build();

    public OrderRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Order save(Order order) {
        String sql = "INSERT INTO orders (user_id, status, total_price, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, order.getUserId());
            ps.setString(2, order.getStatus().name());
            ps.setBigDecimal(3, order.getTotalPrice());
            ps.setTimestamp(4, Timestamp.valueOf(order.getCreatedAt()));
            ps.setTimestamp(5, Timestamp.valueOf(order.getUpdatedAt()));
            return ps;
        }, keyHolder);

        order.setId(((Number) keyHolder.getKeys().get("id")).longValue());
        return order;
    }

    @Override
    public Optional<Order> findById(Long id) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        List<Order> orders = jdbcTemplate.query(sql, rowMapper, id);
        return orders.isEmpty() ? Optional.empty() : Optional.of(orders.get(0));
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    @Override
    public List<Order> findAll() {
        String sql = "SELECT * FROM orders ORDER BY id";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Order update(Order order) {
        String sql = "UPDATE orders SET status = ?, total_price = ?, updated_at = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                order.getStatus().name(),
                order.getTotalPrice(),
                Timestamp.valueOf(order.getUpdatedAt()),
                order.getId());
        return order;
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM orders WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
