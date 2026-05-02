package ma.daba.identity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.daba.common.exception.ConflictException;
import ma.daba.identity.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Internal identity persistence façade — callers from other bounded contexts inject this service only.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterRequest dto, UserRole role, String consentIp) {
        if (userRepository.existsByPhoneNumber(dto.phoneNumber())) {
            throw new ConflictException("PHONE_EXISTS", "Phone number already registered");
        }

        String email = blankToNull(dto.email());
        if (email != null && userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("EMAIL_EXISTS", "Email already registered");
        }

        User user = User.builder()
                .phoneNumber(dto.phoneNumber())
                .email(email)
                .passwordHash(passwordEncoder.encode(dto.password()))
                .role(role)
                .fullName(blankToNull(dto.fullName()))
                .consentGivenAt(Instant.now())
                .consentIpAddress(parseConsentInet(consentIp))
                .build();

        User saved = userRepository.save(user);
        log.info("Registered user id={} role={}", saved.getId(), role);
        return saved;
    }

    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    private static String blankToNull(String s) {
        return StringUtils.hasText(s) ? s.strip() : null;
    }

    /** Parse client IP for {@code inet} persistence; malformed values are omitted (audit-safe). */
    private InetAddress parseConsentInet(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String s = raw.strip();
        if (s.startsWith("[") && s.endsWith("]")) {
            s = s.substring(1, s.length() - 1);
        }
        try {
            return InetAddress.getByName(s);
        } catch (UnknownHostException e) {
            log.warn("Ignoring unparseable consent IP: {}", raw);
            return null;
        }
    }
}
