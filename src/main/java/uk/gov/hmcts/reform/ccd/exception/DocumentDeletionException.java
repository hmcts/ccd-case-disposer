package uk.gov.hmcts.reform.ccd.exception;

public class DocumentDeletionException extends RuntimeException {
    public DocumentDeletionException(final String caseRef, final Throwable cause) {
        super("Error deleting documents for case: " + caseRef, cause);
    }

    public DocumentDeletionException(final int statusCode, final String caseRef) {
        super("Unexpected response code " + statusCode + " while deleting documents for case: " + caseRef);
    }

    public DocumentDeletionException(final String message) {
        super(message);
    }
}
