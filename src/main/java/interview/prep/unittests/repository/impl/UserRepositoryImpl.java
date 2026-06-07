package interview.prep.unittests.repository.impl;

import interview.prep.unittests.entity.User;
import interview.prep.unittests.repository.UserRepository;
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
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> rowMapper = (rs, rowNum) -> User.builder()
            .id(rs.getLong("id"))
            .email(rs.getString("email"))
            .password(rs.getString("password"))
            .firstName(rs.getString("first_name"))
            .lastName(rs.getString("last_name"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
            .build();

    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (email, password, first_name, last_name, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFirstName());
            ps.setString(4, user.getLastName());
            ps.setTimestamp(5, Timestamp.valueOf(user.getCreatedAt()));
            ps.setTimestamp(6, Timestamp.valueOf(user.getUpdatedAt()));
            return ps;
        }, keyHolder);

        user.setId(((Number) keyHolder.getKeys().get("id")).longValue());
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, rowMapper, id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        List<User> users = jdbcTemplate.query(sql, rowMapper, email);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY id";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, password = ?, first_name = ?, last_name = ?, updated_at = ? " +
                     "WHERE id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getPassword(),
                user.getFirstName(),
                user.getLastName(),
                Timestamp.valueOf(user.getUpdatedAt()),
                user.getId());
        return user;
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }
}
