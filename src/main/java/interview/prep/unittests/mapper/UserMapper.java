package interview.prep.unittests.mapper;

import interview.prep.unittests.dto.request.CreateUserRequest;
import interview.prep.unittests.dto.request.RegisterRequest;
import interview.prep.unittests.dto.request.UpdateUserRequest;
import interview.prep.unittests.dto.response.UserResponse;
import interview.prep.unittests.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    public User toEntity(CreateUserRequest request, String encodedPassword) {
        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public User toEntity(RegisterRequest request, String encodedPassword) {
        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public void updateEntity(User user, UpdateUserRequest request) {
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        user.setUpdatedAt(LocalDateTime.now());
    }
}
