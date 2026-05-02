package ma.daba.identity;

import lombok.RequiredArgsConstructor;
import ma.daba.common.exception.UnauthorizedAccessException;
import ma.daba.identity.dto.AuthResponse;
import ma.daba.identity.dto.LoginRequest;
import ma.daba.identity.dto.RegisterRequest;
import ma.daba.identity.dto.UserSummaryResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest request, String clientIp) {
        User saved = userService.register(request, UserRole.CLIENT, clientIp);
        return buildAuthResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userService.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new UnauthorizedAccessException("UNAUTHORIZED", "Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedAccessException("UNAUTHORIZED", "Invalid credentials");
        }

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateAccessToken(user);
        long secs = jwtProperties.getAccessTokenTtl().toSeconds();
        return AuthResponse.of(token, secs, UserSummaryResponse.from(user));
    }
}
