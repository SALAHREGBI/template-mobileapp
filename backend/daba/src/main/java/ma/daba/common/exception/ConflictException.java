package ma.daba.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Business rule violated (duplicate resource, conflicting state).
 */
public class ConflictException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String code;

    public ConflictException(String code, String message, HttpStatus httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public ConflictException(String code, String message) {
        this(code, message, HttpStatus.CONFLICT);
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
