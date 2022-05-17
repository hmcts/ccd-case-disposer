package uk.gov.hmcts.reform.ccd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.parameter.DefaultParameterResolver;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.net.http.HttpClient;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public ParameterResolver provideParameterResolver() {
        return new DefaultParameterResolver();
    }

    @Bean(name = "httpClientDispose")
    public HttpClient httpClientDispose() {
        return HttpClient.newBuilder().build();
    }
}
