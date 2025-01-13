package uk.gov.hmcts.reform.ccd;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.config.ApplicationConfiguration;
import uk.gov.hmcts.reform.ccd.config.ElasticsearchConfiguration;
import uk.gov.hmcts.reform.ccd.config.es.GlobalSearchIndexChecker;
import uk.gov.hmcts.reform.ccd.config.es.TestContainers;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;
import uk.gov.hmcts.reform.ccd.service.remote.DisposeElasticsearchRemoteOperation;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
    ParameterResolver.class,
    ApplicationConfiguration.class,
    ElasticsearchConfiguration.class,
    DisposeElasticsearchRemoteOperation.class,
    GlobalSearchIndexChecker.class
})
@ActiveProfiles("integration")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ApplicationBootstrapIntegrationTest extends TestContainers {
    private static final String FLAG = "true";

    @Test
    void testShouldBootstrapSpringContext() {
        assertThat(Boolean.valueOf(FLAG)).isTrue();
    }
}
