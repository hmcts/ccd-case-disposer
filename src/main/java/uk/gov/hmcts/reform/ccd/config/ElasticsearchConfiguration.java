package uk.gov.hmcts.reform.ccd.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

@Configuration
public class ElasticsearchConfiguration {

    private final ParameterResolver parameterResolver;

    private RestClient restClient;
    private ElasticsearchTransport transport;
    private ElasticsearchClient elasticsearchClient;

    @Inject
    public ElasticsearchConfiguration(final ParameterResolver parameterResolver) {
        this.parameterResolver = parameterResolver;
    }

    @PostConstruct
    public void init() {
        final HttpHost[] httpHosts = parameterResolver.getElasticsearchHosts().stream()
            .map(HttpHost::create)
            .toArray(HttpHost[]::new);
        restClient = RestClient.builder(httpHosts).build();
        transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        elasticsearchClient = new ElasticsearchClient(transport);
    }

    @PreDestroy
    public void cleanUp() throws Exception {
        if (restClient != null) {
            restClient.close();
        }
    }

    @Bean
    public ElasticsearchClient provideElasticsearchClient() {
        return elasticsearchClient;
    }

}
