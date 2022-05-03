package uk.gov.hmcts.reform.ccd;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.config.ApplicationConfiguration;
import uk.gov.hmcts.reform.ccd.config.ElasticsearchConfiguration;
import uk.gov.hmcts.reform.ccd.data.es.CaseDataElasticsearchOperations;
import uk.gov.hmcts.reform.ccd.data.es.TestElasticsearchFixture;
import uk.gov.hmcts.reform.ccd.parameter.ParameterResolver;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        ParameterResolver.class,
        ApplicationConfiguration.class,
        ElasticsearchConfiguration.class,
        CaseDataElasticsearchOperations.class}
)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ApplicationBootstrapIntegrationTest extends TestElasticsearchFixture {
    private static final String FLAG = "true";

    @Test
    void testShouldBootstrapSpringContext() {
        assertThat(Boolean.valueOf(FLAG)).isTrue();
    }

}
