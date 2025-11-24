package uk.gov.hmcts.reform.ccd.config.es;

import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
@ContextConfiguration(initializers = {TestContainers.ElasticsearchInitializer.class})
public abstract class TestContainers {

    protected static final WireMockServer WIREMOCK_SERVER = new WireMockServer(4603);
    private static final Logger LOGGER = LoggerFactory.getLogger(TestContainers.class);
    private static final String IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:7.17.29";
    private static final ElasticsearchContainer ELASTICSEARCH_CONTAINER = new ElasticsearchContainer(IMAGE)
            .withLogConsumer(new Slf4jLogConsumer(LOGGER))
            .waitingFor(Wait.forListeningPort());

    static {
        if (!ELASTICSEARCH_CONTAINER.isRunning()) {
            ELASTICSEARCH_CONTAINER.start();
        }
        if (!WIREMOCK_SERVER.isRunning()) {
            WIREMOCK_SERVER.start();
        }
    }

    public static class ElasticsearchInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "elasticsearch.hosts=" + ELASTICSEARCH_CONTAINER.getHttpHostAddress()
            );
        }
    }
}
