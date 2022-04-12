package uk.gov.hmcts.reform.ccd.util;

public interface Snooper {
    void snoop(String message);

    void snoop(String message, Throwable throwable);
}
