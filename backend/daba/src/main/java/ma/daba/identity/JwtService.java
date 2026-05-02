package ma.daba.identity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    void initSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date exp = Date.from(now.toInstant().plus(jwtProperties.getAccessTokenTtl()));

        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(user.getId().toString())
                .claim("phone", user.getPhoneNumber())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(exp)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Returns the user id encoded in the token; empty if malformed, expired, or wrong issuer/signature.
     */
    public Optional<UUID> tryParseUserId(String token) {
        try {
            String sub = extractAllClaims(token).getSubject();
            return Optional.of(UUID.fromString(sub));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private Claims extractAllClaims(String jwt)
            throws ExpiredJwtException,
            MalformedJwtException,
            io.jsonwebtoken.security.SecurityException,
            io.jsonwebtoken.UnsupportedJwtException {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(jwtProperties.getIssuer())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }
}
