package uk.gov.hmcts.reform.ccd.exception;

public class TasksDeletionException extends RuntimeException {
    public TasksDeletionException(final String caseRef, final Throwable cause) {
        super("Error deleting tasks for case: " + caseRef, cause);
    }

    public TasksDeletionException(final int statusCode, final String caseRef) {
        super("Unexpected response code " + statusCode + " while deleting tasks for case: " + caseRef);
    }
}
