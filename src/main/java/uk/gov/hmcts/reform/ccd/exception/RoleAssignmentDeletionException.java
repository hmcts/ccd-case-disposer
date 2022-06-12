package uk.gov.hmcts.reform.ccd.exception;

public class RoleAssignmentDeletionException extends RuntimeException {
    public RoleAssignmentDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
