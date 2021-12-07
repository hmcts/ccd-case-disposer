package uk.gov.hmcts.reform.ccd;

import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.config.ElasticsearchConfiguration;
import uk.gov.hmcts.reform.ccd.config.TestApplicationConfiguration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = {TestApplicationConfiguration.class, ElasticsearchConfiguration.class})
@ActiveProfiles("functional")
class CaseDeletionFunctionalTest extends TestDataProvider {

    @Autowired
    private ApplicationExecutor executor;

    @BeforeAll
    static void setup() {
        Awaitility.setDefaultPollInterval(1, TimeUnit.SECONDS);
        Awaitility.setDefaultPollDelay(Duration.FIVE_SECONDS);
        Awaitility.setDefaultTimeout(30, TimeUnit.SECONDS);
    }

    @ParameterizedTest
    @MethodSource("provideCaseDeletionScenarios")
    void testSomething(final String deletableCaseTypes,
                       final String scriptPath,
                       final List<Long> initialStateRowIds,
                       final Map<String, List<Long>> indexedData,
                       final List<Long> endStateRowIds,
                       final Map<String, List<Long>> deletedFromIndexed,
                       final Map<String, List<Long>> notDeletedFromIndexed) throws Exception {
        // GIVEN
        setupData(deletableCaseTypes, scriptPath, initialStateRowIds, indexedData);

        // WHEN
        executor.execute();

        // THEN
        verifyDatabaseDeletion(endStateRowIds);
        verifyElasticsearchDeletion(deletedFromIndexed, notDeletedFromIndexed);
    }

}
