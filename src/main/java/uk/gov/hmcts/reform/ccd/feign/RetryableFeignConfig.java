package uk.gov.hmcts.reform.ccd.feign;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RetryableFeignConfig {


    @Bean
    public Retryer feignRetryer() {
        // Example: Retry 3 times, waiting 100ms, then 200ms, then 300ms, etc.
        return new Retryer.Default(100, 200, 3);
    }
}
