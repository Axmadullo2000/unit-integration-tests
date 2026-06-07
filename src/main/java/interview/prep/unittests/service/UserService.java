package interview.prep.unittests.service;

import interview.prep.unittests.dto.request.CreateUserRequest;
import interview.prep.unittests.dto.request.UpdateUserRequest;
import interview.prep.unittests.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);
}
