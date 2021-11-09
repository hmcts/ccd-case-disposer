package uk.gov.hmcts.reform.ccd;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
    basePackages = {"uk.gov.hmcts.reform.ccd"},
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ApplicationRunner.class)
    }
)
@EnableAutoConfiguration
public class TestApplicationConfiguration {


}
