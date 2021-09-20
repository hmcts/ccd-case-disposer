package uk.gov.hmcts.reform.ccd;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;

@Slf4j
@Named
public class ApplicationExecutor {

    public void execute() {
        log.info("Case-Disposer started...");
        log.info("Case-Disposer finished.");
    }

}
