package uk.gov.hmcts.reform.ccd.exception;

public class RoleAssignmentDeletionException extends RuntimeException {
    public RoleAssignmentDeletionException(String caseRef, Throwable cause) {
        super("Error deleting role assignments for case : " + caseRef, cause);
    }

    public RoleAssignmentDeletionException(String message) {
        super(message);
    }

    public RoleAssignmentDeletionException(int statusCode, String caseRef) {
        super("Unexpected response code " + statusCode + " while deleting role assignments for case: " + caseRef);
    }
}
