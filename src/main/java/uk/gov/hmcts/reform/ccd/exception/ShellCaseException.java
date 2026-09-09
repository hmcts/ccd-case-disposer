package uk.gov.hmcts.reform.ccd.exception;

public class ShellCaseException extends RuntimeException {

    public ShellCaseException() {
        super();
    }

    public ShellCaseException(String message) {
        super(message);
    }

    public ShellCaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
