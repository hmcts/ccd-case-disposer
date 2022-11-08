package uk.gov.hmcts.reform.ccd.exception;

public class CaseDisposerAsyncException extends RuntimeException {

    public CaseDisposerAsyncException(final String message, final Throwable cause) {
        super(message, cause);
    }
}