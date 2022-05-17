package uk.gov.hmcts.reform.ccd.exception;

public class DocumentDeletionException extends RuntimeException {
    public DocumentDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
