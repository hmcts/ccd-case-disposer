package uk.gov.hmcts.reform.ccd.exception;

public class LogAndAuditException extends RuntimeException {

    public LogAndAuditException(final String caseRef, final Throwable cause) {
        super("Error posting to Log and Audit for case: " + caseRef, cause);
    }

    public LogAndAuditException(final String message) {
        super(message);
    }

    public LogAndAuditException(final int statusCode, final String caseRef) {
        super("Unexpected response code " + statusCode + " while sending data to Log and Audit for case: " + caseRef);
    }
}
