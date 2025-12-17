package uk.gov.hmcts.reform.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.data.TestDataProvider;
import uk.gov.hmcts.reform.ccd.data.em.CaseDocumentsDeletionResults;
import uk.gov.hmcts.reform.ccd.util.ProcessedCasesRecordHolder;
import uk.gov.hmcts.reform.ccd.utils.DocumentRemoteDeletionVerifier;
import uk.gov.hmcts.reform.ccd.utils.HearingRemoteDeletionVerifier;
import uk.gov.hmcts.reform.ccd.utils.LauRemoteDeletionVerifier;
import uk.gov.hmcts.reform.ccd.utils.RemoteDeletionVerifier;
import uk.gov.hmcts.reform.ccd.utils.RoleRemoteDeletionVerifier;
import uk.gov.hmcts.reform.ccd.utils.TasksRemoteDeletionVerifier;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;


@SpringBootTest
@ActiveProfiles("integration")
@ComponentScan({"uk.gov.hmcts.reform.ccd"})
class CaseDeletionIntegrationTest extends TestDataProvider {

    @Autowired
    private ApplicationExecutor executor;

    @Autowired
    private ProcessedCasesRecordHolder processedCasesRecordHolder;

    @Autowired
    private List<RemoteDeletionVerifier<?>> remoteDeletionVerifiers;

    @Autowired
    private LauRemoteDeletionVerifier lauVerifier;

    @Autowired
    private RoleRemoteDeletionVerifier roleVerifier;

    @Autowired
    private HearingRemoteDeletionVerifier hearingVerifier;

    @Autowired
    private DocumentRemoteDeletionVerifier documentVerifier;

    @Autowired
    private TasksRemoteDeletionVerifier tasksVerifier;

    @BeforeEach
    void resetWireMock() {
        WIREMOCK_SERVER.resetAll();
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
                       final List<Long> deletableCaseRefs,
                       final Map<String, List<Long>> deletedFromIndexed,
                       final Map<String, List<Long>> notDeletedFromIndexed) throws Exception {
        // GIVEN
        setupData(deletableCaseTypes, deletableCaseTypesSimulation, scriptPath, initialStateRowIds, indexedData);
        processedCasesRecordHolder.clearState();

        // WHEN
        executor.execute(1);

        // THEN
        await().atMost(20, SECONDS).untilAsserted(() ->
            verifyAllRemoteDeletions(deletableCaseRefs)
        );

        verifyDatabaseDeletion(deletableEndStateRowIds);
        verifyElasticsearchDeletion(deletedFromIndexed, notDeletedFromIndexed);
        verifyDatabaseDeletionSimulation(simulatedEndStateRowIds);

        // Clear AFTER success
        remoteDeletionVerifiers.forEach(RemoteDeletionVerifier::clear);
    }

    @ParameterizedTest
    @MethodSource("uk.gov.hmcts.reform.ccd.data.DeletionScenarios#provideCaseDeletionScenarios")
    void testScenariosNewVersion(final String deletableCaseTypes,
                       final String deletableCaseTypesSimulation,
                       final String scriptPath,
                       final List<Long> initialStateRowIds,
                       final Map<String, List<Long>> indexedData,
                       final List<Long> deletableEndStateRowIds,
                       final List<Long> simulatedEndStateRowIds,
                       final List<Long> deletableCaseRefs,
                       final Map<String, List<Long>> deletedFromIndexed,
                       final Map<String, List<Long>> notDeletedFromIndexed) throws Exception {
        // GIVEN
        setupData(deletableCaseTypes, deletableCaseTypesSimulation, scriptPath, initialStateRowIds, indexedData);
        processedCasesRecordHolder.clearState();

        // WHEN
        executor.execute(2);

        // THEN
        await().atMost(20, SECONDS).untilAsserted(() ->
            verifyAllRemoteDeletions(deletableCaseRefs)
        );

        verifyDatabaseDeletion(deletableEndStateRowIds);
        verifyElasticsearchDeletion(deletedFromIndexed, notDeletedFromIndexed);
        verifyDatabaseDeletionSimulation(simulatedEndStateRowIds);

        // Clear AFTER success
        remoteDeletionVerifiers.forEach(RemoteDeletionVerifier::clear);
    }

    private void verifyAllRemoteDeletions(List<Long> deletableCaseRefs) {

        // LAU
        Set<String> lauSnapshot = lauVerifier.snapshot();
        lauVerifier.assertDeletionResults(lauSnapshot, deletableCaseRefs);

        // Documents
        Map<String, CaseDocumentsDeletionResults> documentSnapshot = documentVerifier.snapshot();
        documentVerifier.assertDeletionResults(documentSnapshot, deletableCaseRefs);

        // Hearings
        Map<String, Integer> hearingSnapshot = hearingVerifier.snapshot();
        hearingVerifier.assertDeletionResults(hearingSnapshot, deletableCaseRefs);

        // Roles
        Map<String, Integer> roleSnapshot = roleVerifier.snapshot();
        roleVerifier.assertDeletionResults(roleSnapshot, deletableCaseRefs);

        // Tasks
        Map<String, Integer> tasksSnapshot = tasksVerifier.snapshot();
        tasksVerifier.assertDeletionResults(tasksSnapshot, deletableCaseRefs);
    }
}
