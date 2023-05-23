package uk.gov.hmcts.reform.ccd.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

@Configuration
public class ElasticsearchConfiguration {

    private final ParameterResolver parameterResolver;

    private RestHighLevelClient restHighLevelClient;

    @Inject
    public ElasticsearchConfiguration(final ParameterResolver parameterResolver) {
        this.parameterResolver = parameterResolver;
    }

    @PostConstruct
    public void init() {
        final HttpHost[] httpHosts = parameterResolver.getElasticsearchHosts().stream()
            .map(HttpHost::create)
            .toArray(HttpHost[]::new);
        final RestClientBuilder restClientBuilder = RestClient.builder(httpHosts);

        restHighLevelClient = new RestHighLevelClient(restClientBuilder);
    }

    @PreDestroy
    public void cleanUp() throws Exception {
        restHighLevelClient.close();
    }

    @Bean
    public RestHighLevelClient provideRestHighLevelClient() {
        return restHighLevelClient;
    }

}
