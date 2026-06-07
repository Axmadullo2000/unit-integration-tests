package interview.prep.unittests.integration.controller;

import interview.prep.unittests.dto.request.LoginRequest;
import interview.prep.unittests.dto.request.RefreshTokenRequest;
import interview.prep.unittests.dto.request.RegisterRequest;
import interview.prep.unittests.helper.UserTestDataHelper;
import interview.prep.unittests.integration.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @BeforeEach
    void setUp() {
        cleanDatabase();
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class Register {

        @Test
        @DisplayName("should register new user and return tokens")
        void shouldRegisterNewUser() throws Exception {
            // given
            RegisterRequest request = UserTestDataHelper.createRegisterRequest("newuser@example.com");

            // when/then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.expiresIn").isNumber())
                    .andExpect(jsonPath("$.user.email").value("newuser@example.com"))
                    .andExpect(jsonPath("$.user.firstName").value("John"))
                    .andExpect(jsonPath("$.user.lastName").value("Doe"));
        }

        @Test
        @DisplayName("should return 409 when email already exists")
        void shouldReturn409WhenEmailExists() throws Exception {
            // given - register first user
            RegisterRequest request = UserTestDataHelper.createRegisterRequest("duplicate@example.com");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // when/then - try to register with same email
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message").value(containsString("email")));
        }

        @Test
        @DisplayName("should return 400 for invalid request body")
        void shouldReturn400ForInvalidRequest() throws Exception {
            // given - invalid request with empty fields
            RegisterRequest request = RegisterRequest.builder()
                    .email("")
                    .password("123")
                    .firstName("")
                    .lastName("")
                    .build();

            // when/then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errors").isMap())
                    .andExpect(jsonPath("$.errors.email").exists())
                    .andExpect(jsonPath("$.errors.firstName").exists());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("should login with valid credentials")
        void shouldLoginWithValidCredentials() throws Exception {
            // given - register a user first
            RegisterRequest registerRequest = UserTestDataHelper.createRegisterRequest("login@example.com");
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated());

            LoginRequest loginRequest = UserTestDataHelper.createLoginRequest("login@example.com", "password123");

            // when/then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.user.email").value("login@example.com"));
        }

        @Test
        @DisplayName("should return 401 for invalid credentials")
        void shouldReturn401ForInvalidCredentials() throws Exception {
            // given
            LoginRequest loginRequest = UserTestDataHelper.createLoginRequest("nonexistent@example.com", "wrongpassword");

            // when/then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value(containsString("Invalid")));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/refresh")
    class Refresh {

        @Test
        @DisplayName("should refresh token successfully")
        void shouldRefreshTokenSuccessfully() throws Exception {
            // given - register and get refresh token
            RegisterRequest registerRequest = UserTestDataHelper.createRegisterRequest("refresh@example.com");
            String response = mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            String refreshToken = objectMapper.readTree(response).get("refreshToken").asText();

            RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                    .refreshToken(refreshToken)
                    .build();

            // when/then
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());
        }

        @Test
        @DisplayName("should return 401 for invalid refresh token")
        void shouldReturn401ForInvalidRefreshToken() throws Exception {
            // given
            RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                    .refreshToken("invalid-refresh-token")
                    .build();

            // when/then
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout")
    class Logout {

        @Test
        @DisplayName("should logout successfully")
        void shouldLogoutSuccessfully() throws Exception {
            // given - register and get access token
            String accessToken = registerAndGetToken(UserTestDataHelper.createRegisterRequest("logout@example.com"));

            // when/then
            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk());
        }
    }
}
