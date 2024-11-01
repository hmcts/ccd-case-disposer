package uk.gov.hmcts.reform.ccd.exception;

public class HearingDeletionException extends RuntimeException {
    public HearingDeletionException(String message, Throwable cause) {
        super(message, cause);
    }

    public HearingDeletionException(String message) {
        super(message);
    }
}
