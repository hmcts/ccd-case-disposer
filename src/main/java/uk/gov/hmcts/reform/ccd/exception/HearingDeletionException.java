package uk.gov.hmcts.reform.ccd.exception;

public class HearingDeletionException extends RuntimeException {
    public HearingDeletionException(String caseRef, Throwable cause) {
        super("Error deleting hearing for case : " + caseRef, cause);
    }

    public HearingDeletionException(String message) {
        super(message);
    }

    public HearingDeletionException(int statusCode, String caseRef) {
        super("Unexpected response code " + statusCode + " while deleting hearing for case: " + caseRef);
    }
}
