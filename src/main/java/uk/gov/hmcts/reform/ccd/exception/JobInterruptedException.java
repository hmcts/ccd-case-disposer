package uk.gov.hmcts.reform.ccd.exception;


public class JobInterruptedException extends RuntimeException {
    public JobInterruptedException() {
        super("Job interrupted");
    }
}
