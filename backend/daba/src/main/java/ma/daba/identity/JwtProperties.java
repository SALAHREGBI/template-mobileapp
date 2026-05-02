package ma.daba.identity;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "daba.jwt")
public class JwtProperties {

    /** Base64-encoded or raw UTF-8 secret — minimum 32 bytes for HS256 signing. */
    private String secret = "";

    private String issuer = "daba-api";

    /** Access-token time-to-live (ISO-8601 duration / Spring Boot Duration format). */
    private Duration accessTokenTtl = Duration.ofHours(24);

    @PostConstruct
    void assertSecretStrength() {
        if (secret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "daba.jwt.secret must be at least 32 UTF-8 bytes (configure DABA_JWT_SECRET)."
            );
        }
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public void setAccessTokenTtl(Duration accessTokenTtl) {
        this.accessTokenTtl = accessTokenTtl;
    }
}
