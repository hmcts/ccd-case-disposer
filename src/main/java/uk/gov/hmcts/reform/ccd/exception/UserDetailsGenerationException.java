package uk.gov.hmcts.reform.ccd.exception;

public class UserDetailsGenerationException extends RuntimeException {
    public UserDetailsGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UserDetailsGenerationException(final String message) {
        super(message);
    }
}