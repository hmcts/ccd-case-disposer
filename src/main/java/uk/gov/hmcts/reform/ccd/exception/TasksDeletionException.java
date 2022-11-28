package uk.gov.hmcts.reform.ccd.exception;

public class TasksDeletionException extends RuntimeException {
    public TasksDeletionException(String message, Throwable cause) {
        super(message, cause);
    }

    public TasksDeletionException(String message) {
        super(message);
    }
}
