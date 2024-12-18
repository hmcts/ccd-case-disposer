package uk.gov.hmcts.reform.ccd;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.data.TestDataProvider;

import java.util.List;
import java.util.Map;


@SpringBootTest
@ActiveProfiles("test")
@ComponentScan({"uk.gov.hmcts.reform.ccd"})
class CaseDeletionIntegrationTest extends TestDataProvider {

    @Autowired
    private ApplicationExecutor executor;

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

        // WHEN
        executor.execute();

        // THEN
        verifyDatabaseDeletion(deletableEndStateRowIds);
        verifyRemoteDeletion(deletableCaseRefs);
        verifyElasticsearchDeletion(deletedFromIndexed, notDeletedFromIndexed);
        verifyDatabaseDeletionSimulation(simulatedEndStateRowIds);
    }
}
