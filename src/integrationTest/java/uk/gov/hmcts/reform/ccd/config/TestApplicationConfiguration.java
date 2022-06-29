package uk.gov.hmcts.reform.ccd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;


@Configuration
public class TestApplicationConfiguration {

    @Bean
    @Primary
    public ParameterResolver provideTestParameterResolver() {
        return new TestParameterResolver();
    }
}
