package interview.prep.unittests.unit.service;

import interview.prep.unittests.dto.request.CreateUserRequest;
import interview.prep.unittests.dto.request.UpdateUserRequest;
import interview.prep.unittests.dto.response.UserResponse;
import interview.prep.unittests.entity.User;
import interview.prep.unittests.exception.DuplicateResourceException;
import interview.prep.unittests.exception.ResourceNotFoundException;
import interview.prep.unittests.helper.UserTestDataHelper;
import interview.prep.unittests.mapper.UserMapper;
import interview.prep.unittests.repository.UserRepository;
import interview.prep.unittests.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserResponse testUserResponse;
    private CreateUserRequest createUserRequest;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        testUser = UserTestDataHelper.createUser();
        testUserResponse = UserTestDataHelper.createUserResponse();
        createUserRequest = UserTestDataHelper.createCreateUserRequest();
        updateUserRequest = UserTestDataHelper.createUpdateUserRequest();
    }

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("should create user successfully")
        void shouldCreateUserSuccessfully() {
            // given
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userMapper.toEntity(any(CreateUserRequest.class), anyString())).thenReturn(testUser);
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

            // when
            UserResponse result = userService.createUser(createUserRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(testUserResponse.getEmail());
            assertThat(result.getFirstName()).isEqualTo(testUserResponse.getFirstName());

            verify(userRepository).existsByEmail(createUserRequest.getEmail());
            verify(passwordEncoder).encode(createUserRequest.getPassword());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when email exists")
        void shouldThrowWhenEmailExists() {
            // given
            when(userRepository.existsByEmail(anyString())).thenReturn(true);

            // when/then
            assertThatThrownBy(() -> userService.createUser(createUserRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("email");

            verify(userRepository).existsByEmail(createUserRequest.getEmail());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("should return user when found")
        void shouldReturnUserWhenFound() {
            // given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

            // when
            UserResponse result = userService.getUserById(1L);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo(testUserResponse.getEmail());

            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when user not found")
        void shouldThrowWhenUserNotFound() {
            // given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> userService.getUserById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");

            verify(userRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsers {

        @Test
        @DisplayName("should return all users")
        void shouldReturnAllUsers() {
            // given
            User user2 = UserTestDataHelper.createUser(2L, "user2@example.com");
            UserResponse response2 = UserTestDataHelper.createUserResponse(2L, "user2@example.com");

            when(userRepository.findAll()).thenReturn(List.of(testUser, user2));
            when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);
            when(userMapper.toResponse(user2)).thenReturn(response2);

            // when
            List<UserResponse> result = userService.getAllUsers();

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getEmail()).isEqualTo(testUserResponse.getEmail());
            assertThat(result.get(1).getEmail()).isEqualTo("user2@example.com");

            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("should return empty list when no users")
        void shouldReturnEmptyListWhenNoUsers() {
            // given
            when(userRepository.findAll()).thenReturn(Collections.emptyList());

            // when
            List<UserResponse> result = userService.getAllUsers();

            // then
            assertThat(result).isEmpty();

            verify(userRepository).findAll();
        }
    }

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("should update user successfully")
        void shouldUpdateUserSuccessfully() {
            // given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            doNothing().when(userMapper).updateEntity(any(User.class), any(UpdateUserRequest.class));
            when(userRepository.update(any(User.class))).thenReturn(testUser);
            when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

            // when
            UserResponse result = userService.updateUser(1L, updateUserRequest);

            // then
            assertThat(result).isNotNull();

            verify(userRepository).findById(1L);
            verify(userMapper).updateEntity(testUser, updateUserRequest);
            verify(userRepository).update(testUser);
        }

        @Test
        @DisplayName("should update password when provided")
        void shouldUpdatePasswordWhenProvided() {
            // given
            UpdateUserRequest requestWithPassword = UpdateUserRequest.builder()
                    .firstName("Jane")
                    .password("newPassword123")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            doNothing().when(userMapper).updateEntity(any(User.class), any(UpdateUserRequest.class));
            when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");
            when(userRepository.update(any(User.class))).thenReturn(testUser);
            when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

            // when
            userService.updateUser(1L, requestWithPassword);

            // then
            verify(passwordEncoder).encode("newPassword123");
            verify(userRepository).update(testUser);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when user not found")
        void shouldThrowWhenUserNotFound() {
            // given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> userService.updateUser(999L, updateUserRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");

            verify(userRepository).findById(999L);
            verify(userRepository, never()).update(any(User.class));
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("should delete user successfully")
        void shouldDeleteUserSuccessfully() {
            // given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            doNothing().when(userRepository).deleteById(1L);

            // when
            userService.deleteUser(1L);

            // then
            verify(userRepository).findById(1L);
            verify(userRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when user not found")
        void shouldThrowWhenUserNotFound() {
            // given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // when/then
            assertThatThrownBy(() -> userService.deleteUser(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");

            verify(userRepository).findById(999L);
            verify(userRepository, never()).deleteById(anyLong());
        }
    }
}
