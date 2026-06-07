package interview.prep.unittests.helper;

import interview.prep.unittests.dto.request.CreateUserRequest;
import interview.prep.unittests.dto.request.LoginRequest;
import interview.prep.unittests.dto.request.RegisterRequest;
import interview.prep.unittests.dto.request.UpdateUserRequest;
import interview.prep.unittests.dto.response.UserResponse;
import interview.prep.unittests.entity.User;

import java.time.LocalDateTime;

public class UserTestDataHelper {

    public static final String DEFAULT_EMAIL = "test@example.com";
    public static final String DEFAULT_PASSWORD = "password123";
    public static final String DEFAULT_ENCODED_PASSWORD = "encodedPassword";
    public static final String DEFAULT_FIRST_NAME = "John";
    public static final String DEFAULT_LAST_NAME = "Doe";

    // ==================== Entity ====================

    public static User createUser() {
        return createUser(1L, DEFAULT_EMAIL);
    }

    public static User createUser(Long id) {
        return createUser(id, DEFAULT_EMAIL);
    }

    public static User createUser(Long id, String email) {
        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .id(id)
                .email(email)
                .password(DEFAULT_ENCODED_PASSWORD)
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static User.UserBuilder createUserBuilder() {
        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .id(1L)
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_ENCODED_PASSWORD)
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .createdAt(now)
                .updatedAt(now);
    }

    // ==================== Request DTOs ====================

    public static RegisterRequest createRegisterRequest() {
        return RegisterRequest.builder()
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .build();
    }

    public static RegisterRequest createRegisterRequest(String email) {
        return RegisterRequest.builder()
                .email(email)
                .password(DEFAULT_PASSWORD)
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .build();
    }

    public static LoginRequest createLoginRequest() {
        return LoginRequest.builder()
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .build();
    }

    public static LoginRequest createLoginRequest(String email, String password) {
        return LoginRequest.builder()
                .email(email)
                .password(password)
                .build();
    }

    public static CreateUserRequest createCreateUserRequest() {
        return CreateUserRequest.builder()
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .build();
    }

    public static CreateUserRequest createCreateUserRequest(String email) {
        return CreateUserRequest.builder()
                .email(email)
                .password(DEFAULT_PASSWORD)
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .build();
    }

    public static UpdateUserRequest createUpdateUserRequest() {
        return UpdateUserRequest.builder()
                .firstName("UpdatedFirst")
                .lastName("UpdatedLast")
                .build();
    }

    public static UpdateUserRequest createUpdateUserRequest(String firstName, String lastName) {
        return UpdateUserRequest.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

    // ==================== Response DTOs ====================

    public static UserResponse createUserResponse() {
        return createUserResponse(1L, DEFAULT_EMAIL);
    }

    public static UserResponse createUserResponse(Long id, String email) {
        LocalDateTime now = LocalDateTime.now();
        return UserResponse.builder()
                .id(id)
                .email(email)
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static UserResponse createUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
