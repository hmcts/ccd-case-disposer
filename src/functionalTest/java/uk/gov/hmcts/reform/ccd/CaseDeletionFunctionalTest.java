package uk.gov.hmcts.reform.ccd;

import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.config.ElasticsearchConfiguration;
import uk.gov.hmcts.reform.ccd.config.TestApplicationConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ActiveProfiles("functional")
@SpringBootTest(classes = {TestApplicationConfiguration.class, ElasticsearchConfiguration.class})
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam", "uk.gov.hmcts.reform.ccd"})
class CaseDeletionFunctionalTest extends TestDataProvider {

    @Autowired
    private ApplicationExecutor executor;

    @BeforeAll
    static void setup() {
        Awaitility.setDefaultPollInterval(0, TimeUnit.MILLISECONDS);
        Awaitility.setDefaultPollDelay(Durations.FIVE_SECONDS);
        Awaitility.setDefaultTimeout(70, TimeUnit.SECONDS);
    }

    @ParameterizedTest
    @MethodSource("uk.gov.hmcts.reform.ccd.data.DeletionScenarios#provideCaseDeletionScenarios")
    void testScenarios(final String deletableCaseTypes,
                       final String deletableCaseTypesSimulation,
                       final String scriptPath,
                       final List<Long> initialStateRowIds,
                       final Map<String, List<Long>> indexedData,
                       final List<Long> deletableEndStateRowIds,
                       final List<Long> simulatedEndStateRowIds,
                       final Map<Long, List<String>> deletableDocuments,
                       final Map<Long, List<String>> deletableRoles,
                       final Map<String, List<Long>> deletedFromIndexed,
                       final Map<String, List<Long>> notDeletedFromIndexed,
                       final List<Long> deletableRowIds) throws Exception {
        // GIVEN
        setupData(deletableCaseTypes, deletableCaseTypesSimulation, scriptPath, deletableDocuments, deletableRoles,
                initialStateRowIds, indexedData);

        // WHEN
        executor.execute();

        // THEN
        verifyDatabaseDeletion(initialStateRowIds, deletableEndStateRowIds);
        verifyDocumentDeletion(deletableDocuments);
        verifyHearingDocumentDeletion(deletedFromIndexed);
        verifyRoleDeletion(deletableRoles);
        verifyTaskDeletion(deletableRowIds);
        verifyLauLogs(new ArrayList<>(deletedFromIndexed.values()));
        verifyElasticsearchDeletion(deletedFromIndexed, notDeletedFromIndexed);
        verifyDatabaseDeletionSimulation(simulatedEndStateRowIds);
    }
}
