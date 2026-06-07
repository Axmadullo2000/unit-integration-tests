package interview.prep.unittests.repository.impl;

import interview.prep.unittests.entity.RefreshToken;
import interview.prep.unittests.repository.RefreshTokenRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<RefreshToken> rowMapper = (rs, rowNum) -> RefreshToken.builder()
            .id(rs.getLong("id"))
            .userId(rs.getLong("user_id"))
            .token(rs.getString("token"))
            .expiresAt(rs.getTimestamp("expires_at").toLocalDateTime())
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .build();

    public RefreshTokenRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        String sql = "INSERT INTO refresh_tokens (user_id, token, expires_at, created_at) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, refreshToken.getUserId());
            ps.setString(2, refreshToken.getToken());
            ps.setTimestamp(3, Timestamp.valueOf(refreshToken.getExpiresAt()));
            ps.setTimestamp(4, Timestamp.valueOf(refreshToken.getCreatedAt()));
            return ps;
        }, keyHolder);

        refreshToken.setId(((Number) keyHolder.getKeys().get("id")).longValue());
        return refreshToken;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        String sql = "SELECT * FROM refresh_tokens WHERE token = ?";
        List<RefreshToken> tokens = jdbcTemplate.query(sql, rowMapper, token);
        return tokens.isEmpty() ? Optional.empty() : Optional.of(tokens.get(0));
    }

    @Override
    public void deleteByToken(String token) {
        String sql = "DELETE FROM refresh_tokens WHERE token = ?";
        jdbcTemplate.update(sql, token);
    }

    @Override
    public void deleteByUserId(Long userId) {
        String sql = "DELETE FROM refresh_tokens WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    @Override
    public void deleteExpiredTokens() {
        String sql = "DELETE FROM refresh_tokens WHERE expires_at < ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(LocalDateTime.now()));
    }
}
