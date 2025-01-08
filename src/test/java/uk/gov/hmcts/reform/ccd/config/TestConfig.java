package uk.gov.hmcts.reform.ccd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.service.remote.clients.DocumentClient;

import static org.mockito.Mockito.mock;

@Configuration
public class TestConfig {

    @Bean
    public DocumentClient documentClient() {
        return mock(DocumentClient.class);
    }
}
