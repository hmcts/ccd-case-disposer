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
        Awaitility.setDefaultPollDelay(Duration.FIVE_SECONDS);
        Awaitility.setDefaultTimeout(30, TimeUnit.SECONDS);
    }

    @ParameterizedTest
    @MethodSource("uk.gov.hmcts.reform.ccd.data.DeletionScenarios#provideCaseDeletionScenarios")
    void testScenarios(final String deletableCaseTypes,
                       final String deletableCaseTypesSimulation,
                       final Map<String, Integer> initialStateNumberOfDatastoreRecords,
                       final Map<String, Integer> endStateNumberOfDatastoreRecords,
                       final Map<String, String> deletableDocuments,
                       final Map<String, String> deletableRoles) throws Exception {
        // GIVEN
        setupData(deletableCaseTypes, deletableCaseTypesSimulation, initialStateNumberOfDatastoreRecords,
                deletableDocuments, deletableRoles);

        // WHEN
        executor.execute();

        // THEN
        verifyDatabaseDeletion(endStateNumberOfDatastoreRecords);
        verifyDocumentDeletion(deletableDocuments);
        verifyRoleDeletion(deletableRoles);
        verifyElasticsearchDeletion(endStateNumberOfDatastoreRecords);
        verifyDatabaseDeletionSimulation();
    }
}
