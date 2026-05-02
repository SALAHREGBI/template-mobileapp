package ma.daba.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank String phoneNumber,
        @NotBlank @Size(min = 8, max = 128) String password
) {
}
