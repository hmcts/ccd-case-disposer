package uk.gov.hmcts.reform.ccd.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.net.URISyntaxException;

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
        final HttpHost[] httpHosts = parameterResolver.getElasticsearchHosts().stream()
            .map(host -> {
                try {
                    return HttpHost.create(host);
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException("Invalid Elasticsearch host: " + host, e);
                }
            })
            .toArray(HttpHost[]::new);

        Rest5Client restClient = Rest5Client.builder(httpHosts)
            .setConnectionConfigCallback(configBuilder -> configBuilder
                .setConnectTimeout(Timeout.ofMilliseconds(parameterResolver.getElasticsearchRequestTimeout()))
            ).build();
        ElasticsearchTransport transport = new Rest5ClientTransport(restClient, new JacksonJsonpMapper());
        elasticsearchClient = new ElasticsearchClient(transport);
    }

    @Bean(destroyMethod = "close")
    public ElasticsearchClient provideElasticsearchClient() {
        return elasticsearchClient;
    }

}
