package uk.gov.hmcts.reform.ccd.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.ApplicationParameters;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

@Configuration
public class ElasticsearchConfiguration {

    private final ApplicationParameters parameters;

    private RestHighLevelClient restHighLevelClient;

    @Inject
    public ElasticsearchConfiguration(final ApplicationParameters parameters) {
        this.parameters = parameters;
    }

    @PostConstruct
    public void init() {
        final HttpHost[] httpHosts = parameters.getElasticsearchHosts().stream()
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
