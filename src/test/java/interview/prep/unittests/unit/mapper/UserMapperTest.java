package interview.prep.unittests.unit.mapper;

import interview.prep.unittests.dto.request.CreateUserRequest;
import interview.prep.unittests.dto.request.RegisterRequest;
import interview.prep.unittests.dto.request.UpdateUserRequest;
import interview.prep.unittests.dto.response.UserResponse;
import interview.prep.unittests.entity.User;
import interview.prep.unittests.helper.UserTestDataHelper;
import interview.prep.unittests.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Nested
    @DisplayName("toEntity from CreateUserRequest")
    class ToEntityFromCreateUserRequest {

        @Test
        @DisplayName("should map all fields correctly")
        void shouldMapAllFieldsCorrectly() {
            // given
            CreateUserRequest request = UserTestDataHelper.createCreateUserRequest();
            String encodedPassword = "encodedPassword123";

            // when
            User result = userMapper.toEntity(request, encodedPassword);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(request.getEmail());
            assertThat(result.getPassword()).isEqualTo(encodedPassword);
            assertThat(result.getFirstName()).isEqualTo(request.getFirstName());
            assertThat(result.getLastName()).isEqualTo(request.getLastName());
        }

        @Test
        @DisplayName("should set timestamps")
        void shouldSetTimestamps() {
            // given
            CreateUserRequest request = UserTestDataHelper.createCreateUserRequest();
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            // when
            User result = userMapper.toEntity(request, "encodedPassword");

            // then
            assertThat(result.getCreatedAt()).isAfter(before);
            assertThat(result.getUpdatedAt()).isAfter(before);
            assertThat(result.getCreatedAt()).isEqualTo(result.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("toEntity from RegisterRequest")
    class ToEntityFromRegisterRequest {

        @Test
        @DisplayName("should map all fields correctly")
        void shouldMapAllFieldsCorrectly() {
            // given
            RegisterRequest request = UserTestDataHelper.createRegisterRequest();
            String encodedPassword = "encodedPassword123";

            // when
            User result = userMapper.toEntity(request, encodedPassword);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(request.getEmail());
            assertThat(result.getPassword()).isEqualTo(encodedPassword);
            assertThat(result.getFirstName()).isEqualTo(request.getFirstName());
            assertThat(result.getLastName()).isEqualTo(request.getLastName());
        }

        @Test
        @DisplayName("should use encoded password")
        void shouldUseEncodedPassword() {
            // given
            RegisterRequest request = UserTestDataHelper.createRegisterRequest();
            String encodedPassword = "encodedPassword$2a$10$xyz";

            // when
            User result = userMapper.toEntity(request, encodedPassword);

            // then
            assertThat(result.getPassword()).isEqualTo(encodedPassword);
            assertThat(result.getPassword()).isNotEqualTo(request.getPassword());
        }
    }

    @Nested
    @DisplayName("toResponse")
    class ToResponse {

        @Test
        @DisplayName("should map all fields correctly")
        void shouldMapAllFieldsCorrectly() {
            // given
            User user = UserTestDataHelper.createUser();

            // when
            UserResponse result = userMapper.toResponse(user);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(user.getId());
            assertThat(result.getEmail()).isEqualTo(user.getEmail());
            assertThat(result.getFirstName()).isEqualTo(user.getFirstName());
            assertThat(result.getLastName()).isEqualTo(user.getLastName());
            assertThat(result.getCreatedAt()).isEqualTo(user.getCreatedAt());
            assertThat(result.getUpdatedAt()).isEqualTo(user.getUpdatedAt());
        }

        @Test
        @DisplayName("should not include password in response")
        void shouldNotIncludePassword() {
            // given
            User user = UserTestDataHelper.createUser();

            // when
            UserResponse result = userMapper.toResponse(user);

            // then
            // UserResponse class doesn't have password field - this is verified by compilation
            assertThat(result).isNotNull();
            assertThat(result.getClass().getDeclaredFields())
                    .extracting("name")
                    .doesNotContain("password");
        }
    }

    @Nested
    @DisplayName("updateEntity")
    class UpdateEntity {

        @Test
        @DisplayName("should update firstName when provided")
        void shouldUpdateFirstName() {
            // given
            User user = UserTestDataHelper.createUser();
            String originalLastName = user.getLastName();
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .firstName("NewFirstName")
                    .build();

            // when
            userMapper.updateEntity(user, request);

            // then
            assertThat(user.getFirstName()).isEqualTo("NewFirstName");
            assertThat(user.getLastName()).isEqualTo(originalLastName);
        }

        @Test
        @DisplayName("should update lastName when provided")
        void shouldUpdateLastName() {
            // given
            User user = UserTestDataHelper.createUser();
            String originalFirstName = user.getFirstName();
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .lastName("NewLastName")
                    .build();

            // when
            userMapper.updateEntity(user, request);

            // then
            assertThat(user.getLastName()).isEqualTo("NewLastName");
            assertThat(user.getFirstName()).isEqualTo(originalFirstName);
        }

        @Test
        @DisplayName("should not update null fields")
        void shouldNotUpdateNullFields() {
            // given
            User user = UserTestDataHelper.createUser();
            String originalFirstName = user.getFirstName();
            String originalLastName = user.getLastName();
            UpdateUserRequest request = UpdateUserRequest.builder().build();

            // when
            userMapper.updateEntity(user, request);

            // then
            assertThat(user.getFirstName()).isEqualTo(originalFirstName);
            assertThat(user.getLastName()).isEqualTo(originalLastName);
        }

        @Test
        @DisplayName("should update updatedAt timestamp")
        void shouldUpdateTimestamp() {
            // given
            User user = UserTestDataHelper.createUser();
            LocalDateTime originalUpdatedAt = user.getUpdatedAt();
            UpdateUserRequest request = UpdateUserRequest.builder()
                    .firstName("NewFirstName")
                    .build();

            // small delay to ensure timestamp difference
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}

            // when
            userMapper.updateEntity(user, request);

            // then
            assertThat(user.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }
    }
}
