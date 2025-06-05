package uk.gov.hmcts.reform.ccd;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.data.TestDataProvider;
import uk.gov.hmcts.reform.ccd.util.ProcessedCasesRecordHolder;

import java.util.List;
import java.util.Map;


@SpringBootTest
@ActiveProfiles("integration")
@ComponentScan({"uk.gov.hmcts.reform.ccd"})
@Slf4j
class CaseDeletionIntegrationTest extends TestDataProvider {

    @Autowired
    private ApplicationExecutor executor;

    @Autowired
    private ProcessedCasesRecordHolder processedCasesRecordHolder;

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
        log.info("Case types to delete: {}", deletableCaseTypes);
        log.info("Case types for simulated deletion: {}", deletableCaseTypesSimulation);
        log.info("Inserted case ids: {}", initialStateRowIds);
        log.info("Expected case ids to stay: {}", deletableEndStateRowIds);
        log.info("Scenario using script {}", scriptPath);
        log.info("Expected case refs to be deleted: {}", deletableCaseRefs);
        processedCasesRecordHolder.clearState();
        executor.execute();

        // THEN
        verifyDatabaseDeletion(deletableEndStateRowIds);
        verifyRemoteDeletion(deletableCaseRefs);
        verifyElasticsearchDeletion(deletedFromIndexed, notDeletedFromIndexed);
        verifyDatabaseDeletionSimulation(simulatedEndStateRowIds);
    }
}
