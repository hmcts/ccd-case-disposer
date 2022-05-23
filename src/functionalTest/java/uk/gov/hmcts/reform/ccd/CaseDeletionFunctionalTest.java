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

@ActiveProfiles("functional")
@SpringBootTest(classes = {TestApplicationConfiguration.class, ElasticsearchConfiguration.class})
class CaseDeletionFunctionalTest extends TestDataProvider {

    @Autowired
    private ApplicationExecutor executor;

    @BeforeAll
    static void setup() {
        Awaitility.setDefaultPollInterval(0, TimeUnit.MILLISECONDS);
        Awaitility.setDefaultPollDelay(Duration.TWO_SECONDS);
        Awaitility.setDefaultTimeout(30, TimeUnit.SECONDS);
    }

    @ParameterizedTest
    @MethodSource("uk.gov.hmcts.reform.ccd.data.DeletionScenarios#provideCaseDeletionScenarios")
    void testScenarios(final String deletableCaseTypes,
                       final String deletableCaseTypesSimulation,
                       final String ccdScriptPath,
                       final String emScriptPath,
                       final List<Long> initialStateRowIds,
                       final Map<String, List<Long>> indexedData,
                       final List<Long> deletableEndStateRowIds,
                       final List<Long> simulatedEndStateRowIds,
                       final List<String> deletableDocumentIds,
                       final Map<String, List<Long>> deletedFromIndexed,
                       final Map<String, List<Long>> notDeletedFromIndexed) throws Exception {
        // GIVEN
        setupData(deletableCaseTypes, deletableCaseTypesSimulation, ccdScriptPath, emScriptPath,
                  initialStateRowIds, indexedData);

        // WHEN
        executor.execute();

        // THEN
        verifyDatabaseDeletion(deletableEndStateRowIds);
        verifyEvidenceDatabaseDeletion(deletableDocumentIds);
        verifyElasticsearchDeletion(deletedFromIndexed, notDeletedFromIndexed);
        verifyDatabaseDeletionSimulation(simulatedEndStateRowIds);
    }
}
