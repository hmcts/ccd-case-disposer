package uk.gov.hmcts.reform.ccd.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

@Slf4j
@Configuration
public class ElasticsearchConfiguration {

    private final ParameterResolver parameterResolver;
    private ElasticsearchClient elasticsearchClient;

    @Inject
    public ElasticsearchConfiguration(final ParameterResolver parameterResolver) {
        this.parameterResolver = parameterResolver;
    }

    @PostConstruct
    public void init() {
        final var resolvedHosts = parameterResolver.getElasticsearchHosts();
        final String marker = "[ES-CONFIG] Initialising Elasticsearch client with resolved hosts: " + resolvedHosts;
        log.info(marker);
        System.err.println(marker);
        final HttpHost[] httpHosts = resolvedHosts.stream()
            .map(HttpHost::create)
            .toArray(HttpHost[]::new);

        RestClientBuilder builder = RestClient.builder(httpHosts)
            .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                .setConnectTimeout(parameterResolver.getElasticsearchRequestTimeout())
            );
        ElasticsearchTransport transport = new RestClientTransport(builder.build(), new JacksonJsonpMapper());
        elasticsearchClient = new ElasticsearchClient(transport);
    }

    @Bean(destroyMethod = "close")
    public ElasticsearchClient provideElasticsearchClient() {
        return elasticsearchClient;
    }

}
