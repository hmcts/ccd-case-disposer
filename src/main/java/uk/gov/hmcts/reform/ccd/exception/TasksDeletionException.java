package uk.gov.hmcts.reform.ccd.exception;

public class TasksDeletionException extends RuntimeException {
    public TasksDeletionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public TasksDeletionException(final String message) {
        super(message);
    }
}
