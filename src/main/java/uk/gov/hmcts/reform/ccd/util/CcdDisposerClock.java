package uk.gov.hmcts.reform.ccd.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class CcdDisposerClock {

    @Bean
    public Clock utcClock() {
        return Clock.systemUTC();
    }
}
