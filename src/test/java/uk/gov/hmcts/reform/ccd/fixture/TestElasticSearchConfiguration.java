package uk.gov.hmcts.reform.ccd.fixture;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@TestConfiguration
public class TestElasticSearchConfiguration {

    private static final String ELASTICSEARCH_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:7.14.1";

    private final ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(ELASTICSEARCH_IMAGE)
        .waitingFor(Wait.forListeningPort());

    @PostConstruct
    void init() {
        if (!elasticsearchContainer.isRunning()) {
            elasticsearchContainer.start();
        }
    }

    @PreDestroy
    void cleanUp() {
        if (elasticsearchContainer.isRunning()) {
            elasticsearchContainer.stop();
        }
    }

    @Bean
    public RestHighLevelClient provideRestHighLevelClient() {
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
            .connectedTo(elasticsearchContainer.getHttpHostAddress())
            .build();

        return RestClients.create(clientConfiguration).rest();
    }

    @Bean
    public ElasticsearchOperations provideElasticsearchOperations(final RestHighLevelClient restHighLevelClient) {
        return new ElasticsearchRestTemplate(restHighLevelClient);
    }

}
