package uk.gov.hmcts.reform.ccd.exception;

public class ServiceAuthTokenGenerationException extends RuntimeException {
    public ServiceAuthTokenGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ServiceAuthTokenGenerationException(final String message) {
        super(message);
    }
}
