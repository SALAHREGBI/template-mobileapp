package ma.daba.identity;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ma.daba.common.api.ApiError;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

/**
 * Stateless bearer-token authentication for REST calls.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_SCHEME = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private static boolean isPublicRequest(HttpServletRequest request) {
        String path = request.getServletPath();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (path.startsWith("/api/v1/auth")) {
            return true;
        }
        if ("/actuator/health".equals(path) && "GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (path.startsWith("/ws")) {
            return true;
        }
        return "/error".equals(path);
    }

    private static Optional<String> readBearer(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(AUTHORIZATION_SCHEME)) {
            return Optional.empty();
        }
        return Optional.of(header.substring(AUTHORIZATION_SCHEME.length()).trim());
    }

    private void writeJson(HttpServletResponse response, ApiError apiError, int status)
            throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), apiError);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (isPublicRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<String> maybeToken = readBearer(request);
        if (maybeToken.isEmpty()) {
            writeJson(response, new ApiError(HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED", "Missing Bearer token"),
                    HttpStatus.UNAUTHORIZED.value());
            return;
        }

        Optional<UUID> userIdOpt = jwtService.tryParseUserId(maybeToken.get());
        if (userIdOpt.isEmpty()) {
            writeJson(response, new ApiError(HttpStatus.UNAUTHORIZED.value(), "INVALID_TOKEN",
                    "Token is expired or invalid"),
                    HttpStatus.UNAUTHORIZED.value());
            return;
        }

        User user = userRepository.findById(userIdOpt.get()).orElse(null);
        if (user == null) {
            writeJson(response, new ApiError(HttpStatus.UNAUTHORIZED.value(), "UNKNOWN_PRINCIPAL",
                    "User no longer exists"),
                    HttpStatus.UNAUTHORIZED.value());
            return;
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            writeJson(response, new ApiError(HttpStatus.FORBIDDEN.value(), "FORBIDDEN", "Account disabled"),
                    HttpStatus.FORBIDDEN.value());
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
