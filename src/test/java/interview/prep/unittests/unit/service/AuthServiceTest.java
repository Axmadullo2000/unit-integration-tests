package interview.prep.unittests.unit.service;

import interview.prep.unittests.dto.request.LoginRequest;
import interview.prep.unittests.dto.request.RefreshTokenRequest;
import interview.prep.unittests.dto.request.RegisterRequest;
import interview.prep.unittests.dto.response.AuthResponse;
import interview.prep.unittests.dto.response.UserResponse;
import interview.prep.unittests.entity.RefreshToken;
import interview.prep.unittests.entity.User;
import interview.prep.unittests.exception.DuplicateResourceException;
import interview.prep.unittests.exception.UnauthorizedException;
import interview.prep.unittests.helper.RefreshTokenTestDataHelper;
import interview.prep.unittests.helper.UserTestDataHelper;
import interview.prep.unittests.mapper.UserMapper;
import interview.prep.unittests.repository.RefreshTokenRepository;
import interview.prep.unittests.repository.UserRepository;
import interview.prep.unittests.security.JwtTokenProvider;
import interview.prep.unittests.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Create user /api/auth")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private UserResponse testUserResponse;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private RefreshTokenRequest refreshTokenRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUserResponse = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        refreshTokenRequest = RefreshTokenRequest.builder()
                .refreshToken("valid-refresh-token")
                .build();
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("should register user successfully")
        void shouldRegisterUserSuccessfully() {
            // given
            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userMapper.toEntity(registerRequest, "encodedPassword")).thenReturn(testUser);
            when(userRepository.save(testUser)).thenReturn(testUser);
            when(jwtTokenProvider.generateAccessToken("test@example.com", 1L)).thenReturn("access-token");
            when(jwtTokenProvider.generateRefreshToken("test@example.com", 1L)).thenReturn("refresh-token");
            when(jwtTokenProvider.getRefreshTokenExpiration()).thenReturn(604800000L);
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(3600000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(RefreshTokenTestDataHelper.createRefreshToken());
            when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

            // when
            AuthResponse response = authService.register(registerRequest);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getExpiresIn()).isEqualTo(3600L);
            assertThat(response.getUser()).isEqualTo(testUserResponse);

            verify(userRepository).existsByEmail("test@example.com");
            verify(passwordEncoder).encode("password123");
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when email exists")
        void shouldThrowWhenEmailExists() {
            // given
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("email");

            verify(userRepository).existsByEmail("test@example.com");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("should return access and refresh tokens")
        void shouldReturnTokens() {
            // given
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userMapper.toEntity(any(RegisterRequest.class), anyString())).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtTokenProvider.generateAccessToken(anyString(), anyLong())).thenReturn("access-token");
            when(jwtTokenProvider.generateRefreshToken(anyString(), anyLong())).thenReturn("refresh-token");
            when(jwtTokenProvider.getRefreshTokenExpiration()).thenReturn(604800000L);
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(900000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(RefreshTokenTestDataHelper.createRefreshToken());
            when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

            // when
            AuthResponse result = authService.register(registerRequest);

            // then
            assertThat(result.getAccessToken()).isNotBlank();
            assertThat(result.getRefreshToken()).isNotBlank();
            assertThat(result.getTokenType()).isEqualTo("Bearer");
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("should login successfully with valid credentials")
        void shouldLoginSuccessfully() {
            // given
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
            when(jwtTokenProvider.generateAccessToken("test@example.com", 1L)).thenReturn("access-token");
            when(jwtTokenProvider.generateRefreshToken("test@example.com", 1L)).thenReturn("refresh-token");
            when(jwtTokenProvider.getRefreshTokenExpiration()).thenReturn(604800000L);
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(900000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(RefreshTokenTestDataHelper.createRefreshToken());
            when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

            // when
            AuthResponse result = authService.login(loginRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("access-token");
            assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(result.getUser().getEmail()).isEqualTo("test@example.com");

            verify(passwordEncoder).matches("password123", "encodedPassword");
        }

        @Test
        @DisplayName("should throw UnauthorizedException when email not found")
        void shouldThrowWhenEmailNotFound() {
            // given
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Invalid");

            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("should throw UnauthorizedException when password is wrong")
        void shouldThrowWhenPasswordIsWrong() {
            // given
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

            // when/then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Invalid");

            verify(jwtTokenProvider, never()).generateAccessToken(anyString(), anyLong());
        }
    }

    @Nested
    @DisplayName("refreshToken")
    class RefreshTokenTests {

        @Test
        @DisplayName("should refresh token successfully")
        void shouldRefreshTokenSuccessfully() {
            // given
            RefreshToken validToken = RefreshTokenTestDataHelper.createRefreshToken(1L, 1L);

            when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(validToken));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            doNothing().when(refreshTokenRepository).deleteByToken("valid-refresh-token");
            when(jwtTokenProvider.generateAccessToken("test@example.com", 1L)).thenReturn("new-access-token");
            when(jwtTokenProvider.generateRefreshToken("test@example.com", 1L)).thenReturn("new-refresh-token");
            when(jwtTokenProvider.getRefreshTokenExpiration()).thenReturn(604800000L);
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(900000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(validToken);
            when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

            // when
            AuthResponse result = authService.refreshToken(refreshTokenRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("new-access-token");
            assertThat(result.getRefreshToken()).isEqualTo("new-refresh-token");
        }

        @Test
        @DisplayName("should throw UnauthorizedException when refresh token not found")
        void shouldThrowWhenRefreshTokenNotFound() {
            // given
            when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> authService.refreshToken(refreshTokenRequest))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Invalid");

            verify(userRepository, never()).findById(anyLong());
        }

        @Test
        @DisplayName("should throw UnauthorizedException when refresh token expired")
        void shouldThrowWhenRefreshTokenExpired() {
            // given
            RefreshToken expiredToken = RefreshTokenTestDataHelper.createExpiredRefreshToken(1L, 1L);

            when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(expiredToken));
            doNothing().when(refreshTokenRepository).deleteByToken("valid-refresh-token");

            // when/then
            assertThatThrownBy(() -> authService.refreshToken(refreshTokenRequest))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("expired");

            verify(refreshTokenRepository).deleteByToken("valid-refresh-token");
        }

        @Test
        @DisplayName("should delete old refresh token after use")
        void shouldDeleteOldRefreshToken() {
            // given
            RefreshToken validToken = RefreshTokenTestDataHelper.createRefreshToken(1L, 1L);

            when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(validToken));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            doNothing().when(refreshTokenRepository).deleteByToken("valid-refresh-token");
            when(jwtTokenProvider.generateAccessToken(anyString(), anyLong())).thenReturn("new-access-token");
            when(jwtTokenProvider.generateRefreshToken(anyString(), anyLong())).thenReturn("new-refresh-token");
            when(jwtTokenProvider.getRefreshTokenExpiration()).thenReturn(604800000L);
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(900000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(validToken);
            when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

            // when
            authService.refreshToken(refreshTokenRequest);

            // then
            verify(refreshTokenRepository).deleteByToken("valid-refresh-token");
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("should logout successfully")
        void shouldLogoutSuccessfully() {
            // given
            String token = "valid-token";
            when(jwtTokenProvider.validateToken(token)).thenReturn(true);
            when(jwtTokenProvider.getUserIdFromToken(token)).thenReturn(1L);
            doNothing().when(refreshTokenRepository).deleteByUserId(1L);

            // when
            authService.logout(token);

            // then
            verify(refreshTokenRepository).deleteByUserId(1L);
        }

        @Test
        @DisplayName("should handle Bearer prefix in token")
        void shouldHandleBearerPrefix() {
            // given
            String tokenWithBearer = "Bearer valid-token";
            when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
            when(jwtTokenProvider.getUserIdFromToken("valid-token")).thenReturn(1L);
            doNothing().when(refreshTokenRepository).deleteByUserId(1L);

            // when
            authService.logout(tokenWithBearer);

            // then
            verify(jwtTokenProvider).validateToken("valid-token");
            verify(refreshTokenRepository).deleteByUserId(1L);
        }
    }
}
