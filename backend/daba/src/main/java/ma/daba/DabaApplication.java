package ma.daba;

import ma.daba.identity.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Modular monolith entrypoint — enable feature packages under {@code ma.daba.*}.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableConfigurationProperties(JwtProperties.class)
@EnableScheduling
public class DabaApplication {

    public static void main(String[] args) {
        SpringApplication.run(DabaApplication.class, args);
    }
}
