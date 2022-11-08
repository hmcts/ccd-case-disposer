package uk.gov.hmcts.reform.ccd.exception;

public class IdamAuthTokenGenerationException extends RuntimeException {
    public IdamAuthTokenGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdamAuthTokenGenerationException(String message) {
        super(message);
    }
}
