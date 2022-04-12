package uk.gov.hmcts.reform.ccd.exception;

public class CaseDataNotFound extends RuntimeException {
    public CaseDataNotFound(String message) {
        super(message);
    }
}
