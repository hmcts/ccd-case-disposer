package uk.gov.hmcts.reform.ccd.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import java.net.URISyntaxException;

@Configuration
public class ElasticsearchConfiguration {

    @Bean(destroyMethod = "close")
    public Rest5Client restClient(ParameterResolver parameterResolver) {
        final HttpHost[] httpHosts = parameterResolver.getElasticsearchHosts().stream()
            .map(host -> {
                try {
                    return HttpHost.create(host);
                } catch (URISyntaxException e) {
                    throw new IllegalArgumentException("Invalid Elasticsearch host: " + host, e);
                }
            })
            .toArray(HttpHost[]::new);

        return Rest5Client.builder(httpHosts)
            .setConnectionConfigCallback(configBuilder -> configBuilder
                .setConnectTimeout(Timeout.ofMilliseconds(parameterResolver.getElasticsearchRequestTimeout()))
            ).build();
    }

    @Bean(destroyMethod = "close")
    public ElasticsearchTransport elasticsearchTransport(Rest5Client restClient) {
        return new Rest5ClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }

}
