package uk.gov.hmcts.reform.ccd.util;

import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;

@Named
@Slf4j
public class DefaultSnooper implements Snooper {

    @Override
    public void snoop(final String message) {
        log.info(message);
    }

    @Override
    public void snoop(final String message, final Throwable throwable) {
        log.error(message, throwable);
    }
}
