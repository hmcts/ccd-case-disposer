package uk.gov.hmcts.reform.ccd.exception;

public class DocumentDeletionException extends RuntimeException {
    public DocumentDeletionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DocumentDeletionException(final String message) {
        super(message);
    }
}
