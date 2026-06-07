package interview.prep.unittests.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import interview.prep.unittests.dto.request.RegisterRequest;
import interview.prep.unittests.dto.response.AuthResponse;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for integration tests.
 * Provides common utilities for test setup and authentication.
 * Uses 'test' profile → connects to booking_db_test database.
 * Automatically cleans up database after each test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    /**
     * Cleans all tables after each test.
     * Ensures no test data remains in the database.
     */
    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    /**
     * Cleans all tables in the database.
     */
    protected void cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM order_items");
        jdbcTemplate.execute("DELETE FROM orders");
        jdbcTemplate.execute("DELETE FROM refresh_tokens");
        jdbcTemplate.execute("DELETE FROM users");
    }

    /**
     * Registers a user and returns the access token.
     */
    protected String registerAndGetToken(RegisterRequest request) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponse.class
        );

        return authResponse.getAccessToken();
    }

    /**
     * Registers a default test user and returns the access token.
     */
    protected String registerDefaultUserAndGetToken() throws Exception {
        return registerAndGetToken(TestDataFactory.createRegisterRequest());
    }

    /**
     * Creates the Authorization header value with Bearer token.
     */
    protected String bearerToken(String token) {
        return "Bearer " + token;
    }
}
