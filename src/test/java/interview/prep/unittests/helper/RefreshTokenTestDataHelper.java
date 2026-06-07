package interview.prep.unittests.helper;

import interview.prep.unittests.dto.request.RefreshTokenRequest;
import interview.prep.unittests.entity.RefreshToken;

import java.time.LocalDateTime;

public class RefreshTokenTestDataHelper {

    public static final String DEFAULT_TOKEN = "valid-refresh-token";
    public static final Long DEFAULT_USER_ID = 1L;

    // ==================== Entity ====================

    public static RefreshToken createRefreshToken() {
        return createRefreshToken(1L, DEFAULT_USER_ID);
    }

    public static RefreshToken createRefreshToken(Long id) {
        return createRefreshToken(id, DEFAULT_USER_ID);
    }

    public static RefreshToken createRefreshToken(Long id, Long userId) {
        return RefreshToken.builder()
                .id(id)
                .userId(userId)
                .token(DEFAULT_TOKEN)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static RefreshToken createRefreshToken(Long id, Long userId, String token) {
        return RefreshToken.builder()
                .id(id)
                .userId(userId)
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static RefreshToken createExpiredRefreshToken(Long id, Long userId) {
        return RefreshToken.builder()
                .id(id)
                .userId(userId)
                .token(DEFAULT_TOKEN)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now().minusDays(8))
                .build();
    }

    public static RefreshToken.RefreshTokenBuilder createRefreshTokenBuilder() {
        return RefreshToken.builder()
                .id(1L)
                .userId(DEFAULT_USER_ID)
                .token(DEFAULT_TOKEN)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now());
    }

    // ==================== Request DTOs ====================

    public static RefreshTokenRequest createRefreshTokenRequest() {
        return RefreshTokenRequest.builder()
                .refreshToken(DEFAULT_TOKEN)
                .build();
    }

    public static RefreshTokenRequest createRefreshTokenRequest(String token) {
        return RefreshTokenRequest.builder()
                .refreshToken(token)
                .build();
    }
}
