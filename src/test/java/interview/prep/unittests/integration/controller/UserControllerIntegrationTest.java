package interview.prep.unittests.integration.controller;

import interview.prep.unittests.dto.request.UpdateUserRequest;
import interview.prep.unittests.helper.UserTestDataHelper;
import interview.prep.unittests.integration.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerIntegrationTest extends BaseIntegrationTest {

    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();
        accessToken = registerDefaultUserAndGetToken();
    }

    @Nested
    @DisplayName("GET /api/users")
    class GetAllUsers {

        @Test
        @DisplayName("should return all users when authenticated")
        void shouldReturnAllUsers() throws Exception {
            // given - register another user
            registerAndGetToken(UserTestDataHelper.createRegisterRequest("user2@example.com"));

            // when/then
            mockMvc.perform(get("/api/users")
                            .header("Authorization", bearerToken(accessToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].email", containsInAnyOrder("test@example.com", "user2@example.com")));
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // when/then
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/users/{id}")
    class GetUserById {

        @Test
        @DisplayName("should return user when found")
        void shouldReturnUserWhenFound() throws Exception {
            // given - get user id from database
            String usersResponse = mockMvc.perform(get("/api/users")
                            .header("Authorization", bearerToken(accessToken)))
                    .andReturn().getResponse().getContentAsString();

            Long userId = objectMapper.readTree(usersResponse).get(0).get("id").asLong();

            // when/then
            mockMvc.perform(get("/api/users/{id}", userId)
                            .header("Authorization", bearerToken(accessToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userId))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"));
        }

        @Test
        @DisplayName("should return 404 when user not found")
        void shouldReturn404WhenNotFound() throws Exception {
            // when/then
            mockMvc.perform(get("/api/users/{id}", 99999)
                            .header("Authorization", bearerToken(accessToken)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value(containsString("99999")));
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{id}")
    class UpdateUser {

        @Test
        @DisplayName("should update user successfully")
        void shouldUpdateUserSuccessfully() throws Exception {
            // given
            String usersResponse = mockMvc.perform(get("/api/users")
                            .header("Authorization", bearerToken(accessToken)))
                    .andReturn().getResponse().getContentAsString();

            Long userId = objectMapper.readTree(usersResponse).get(0).get("id").asLong();

            UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                    .firstName("UpdatedJohn")
                    .lastName("UpdatedDoe")
                    .build();

            // when/then
            mockMvc.perform(put("/api/users/{id}", userId)
                            .header("Authorization", bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("UpdatedJohn"))
                    .andExpect(jsonPath("$.lastName").value("UpdatedDoe"));
        }

        @Test
        @DisplayName("should return 400 for invalid request")
        void shouldReturn400ForInvalidRequest() throws Exception {
            // given
            String usersResponse = mockMvc.perform(get("/api/users")
                            .header("Authorization", bearerToken(accessToken)))
                    .andReturn().getResponse().getContentAsString();

            Long userId = objectMapper.readTree(usersResponse).get(0).get("id").asLong();

            // firstName exceeds max length
            UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                    .firstName("A".repeat(100))
                    .build();

            // when/then
            mockMvc.perform(put("/api/users/{id}", userId)
                            .header("Authorization", bearerToken(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.firstName").exists());
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{id}")
    class DeleteUser {

        @Test
        @DisplayName("should delete user successfully")
        void shouldDeleteUserSuccessfully() throws Exception {
            // given - create another user to delete
            String token2 = registerAndGetToken(UserTestDataHelper.createRegisterRequest("todelete@example.com"));

            String usersResponse = mockMvc.perform(get("/api/users")
                            .header("Authorization", bearerToken(accessToken)))
                    .andReturn().getResponse().getContentAsString();

            // Find the user to delete (not the first one)
            Long userIdToDelete = null;
            for (var node : objectMapper.readTree(usersResponse)) {
                if (node.get("email").asText().equals("todelete@example.com")) {
                    userIdToDelete = node.get("id").asLong();
                    break;
                }
            }

            // when/then
            mockMvc.perform(delete("/api/users/{id}", userIdToDelete)
                            .header("Authorization", bearerToken(accessToken)))
                    .andExpect(status().isNoContent());

            // verify user is deleted
            mockMvc.perform(get("/api/users/{id}", userIdToDelete)
                            .header("Authorization", bearerToken(accessToken)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 404 when user not found")
        void shouldReturn404WhenNotFound() throws Exception {
            // when/then
            mockMvc.perform(delete("/api/users/{id}", 99999)
                            .header("Authorization", bearerToken(accessToken)))
                    .andExpect(status().isNotFound());
        }
    }
}
