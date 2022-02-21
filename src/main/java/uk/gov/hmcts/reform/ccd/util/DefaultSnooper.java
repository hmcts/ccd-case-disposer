package uk.gov.hmcts.reform.ccd.util;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;

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
