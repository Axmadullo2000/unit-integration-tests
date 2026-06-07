package interview.prep.unittests.service;

import interview.prep.unittests.dto.request.LoginRequest;
import interview.prep.unittests.dto.request.RefreshTokenRequest;
import interview.prep.unittests.dto.request.RegisterRequest;
import interview.prep.unittests.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String token);
}
