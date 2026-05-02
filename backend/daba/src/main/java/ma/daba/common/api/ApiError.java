package ma.daba.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String code,
        String message,
        Map<String, String> errors
) {
    public ApiError(int status, String code, String message) {
        this(OffsetDateTime.now(), status, code, message, null);
    }
}
