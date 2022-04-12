package uk.gov.hmcts.reform.ccd.exception;

public class CaseDeletionException extends RuntimeException {
    public CaseDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
