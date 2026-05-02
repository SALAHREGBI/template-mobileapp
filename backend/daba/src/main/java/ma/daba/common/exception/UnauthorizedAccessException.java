package ma.daba.common.exception;

/**
 * Equivalent to incorrect credentials without leaking whether the account exists.
 */
public class UnauthorizedAccessException extends RuntimeException {

    private final String code;

    public UnauthorizedAccessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
