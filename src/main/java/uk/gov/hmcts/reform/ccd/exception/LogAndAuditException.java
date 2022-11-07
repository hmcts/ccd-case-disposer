package uk.gov.hmcts.reform.ccd.exception;

public class LogAndAuditException extends RuntimeException {

    public LogAndAuditException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public LogAndAuditException(final String message) {
        super(message);
    }
}
