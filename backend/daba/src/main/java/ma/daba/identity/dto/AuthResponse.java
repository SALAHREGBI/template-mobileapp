package ma.daba.identity.dto;

public record AuthResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds,
        UserSummaryResponse user
) {
    public static AuthResponse of(String token, long expiresInSeconds, UserSummaryResponse user) {
        return new AuthResponse("Bearer", token, expiresInSeconds, user);
    }
}
