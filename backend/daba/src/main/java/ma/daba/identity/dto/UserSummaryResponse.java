package ma.daba.identity.dto;

import ma.daba.identity.User;
import ma.daba.identity.UserRole;

import java.util.UUID;

public record UserSummaryResponse(UUID id, String phoneNumber, String email, String fullName,
                                  UserRole role, boolean verified) {

    public static UserSummaryResponse from(User u) {
        return new UserSummaryResponse(
                u.getId(),
                u.getPhoneNumber(),
                u.getEmail(),
                u.getFullName(),
                u.getRole(),
                Boolean.TRUE.equals(u.getIsVerified())
        );
    }
}
