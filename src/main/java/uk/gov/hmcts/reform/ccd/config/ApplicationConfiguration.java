package uk.gov.hmcts.reform.ccd.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import uk.gov.hmcts.reform.ccd.ApplicationParameters;

import javax.inject.Inject;

@Configuration
public class ApplicationConfiguration {

    private final ApplicationParameters parameters;

    @Inject
    public ApplicationConfiguration(final ApplicationParameters parameters) {
        this.parameters = parameters;
    }

    @Bean
    public RestHighLevelClient provideRestHighLevelClient() {
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
            .connectedTo(parameters.getElasticsearchHosts())
            .withConnectTimeout(parameters.getElasticsearchRequestTimeout())
            .build();

        return RestClients.create(clientConfiguration).rest();
    }

    @Bean
    public ElasticsearchOperations provideElasticsearchOperations(final RestHighLevelClient restHighLevelClient) {
        return new ElasticsearchRestTemplate(restHighLevelClient);
    }
}
