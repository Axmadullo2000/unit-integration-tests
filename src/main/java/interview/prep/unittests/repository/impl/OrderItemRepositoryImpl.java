package interview.prep.unittests.repository.impl;

import interview.prep.unittests.entity.OrderItem;
import interview.prep.unittests.repository.OrderItemRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderItemRepositoryImpl implements OrderItemRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<OrderItem> rowMapper = (rs, rowNum) -> OrderItem.builder()
            .id(rs.getLong("id"))
            .orderId(rs.getLong("order_id"))
            .eventName(rs.getString("event_name"))
            .eventDate(rs.getTimestamp("event_date").toLocalDateTime())
            .venue(rs.getString("venue"))
            .seatNumber(rs.getString("seat_number"))
            .price(rs.getBigDecimal("price"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .build();

    public OrderItemRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OrderItem save(OrderItem orderItem) {
        String sql = "INSERT INTO order_items (order_id, event_name, event_date, venue, seat_number, price, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, orderItem.getOrderId());
            ps.setString(2, orderItem.getEventName());
            ps.setTimestamp(3, Timestamp.valueOf(orderItem.getEventDate()));
            ps.setString(4, orderItem.getVenue());
            ps.setString(5, orderItem.getSeatNumber());
            ps.setBigDecimal(6, orderItem.getPrice());
            ps.setTimestamp(7, Timestamp.valueOf(orderItem.getCreatedAt()));
            return ps;
        }, keyHolder);

        orderItem.setId(((Number) keyHolder.getKeys().get("id")).longValue());
        return orderItem;
    }

    @Override
    public List<OrderItem> saveAll(List<OrderItem> orderItems) {
        List<OrderItem> savedItems = new ArrayList<>();
        for (OrderItem item : orderItems) {
            savedItems.add(save(item));
        }
        return savedItems;
    }

    @Override
    public Optional<OrderItem> findById(Long id) {
        String sql = "SELECT * FROM order_items WHERE id = ?";
        List<OrderItem> items = jdbcTemplate.query(sql, rowMapper, id);
        return items.isEmpty() ? Optional.empty() : Optional.of(items.get(0));
    }

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        String sql = "SELECT * FROM order_items WHERE order_id = ? ORDER BY id";
        return jdbcTemplate.query(sql, rowMapper, orderId);
    }

    @Override
    public void deleteByOrderId(Long orderId) {
        String sql = "DELETE FROM order_items WHERE order_id = ?";
        jdbcTemplate.update(sql, orderId);
    }
}
