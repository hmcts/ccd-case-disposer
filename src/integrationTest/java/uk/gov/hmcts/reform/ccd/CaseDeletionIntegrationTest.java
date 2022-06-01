package uk.gov.hmcts.reform.ccd;

import java.util.List;
import java.util.Map;

//@Ignore
//@SpringBootTest
//@ActiveProfiles("test")
//@ComponentScan({"uk.gov.hmcts.reform.ccd"})
class CaseDeletionIntegrationTest {

    //@Autowired
    //private ApplicationExecutor executor;


    //@ParameterizedTest
    //@MethodSource("uk.gov.hmcts.reform.ccd.data.DeletionScenarios#provideCaseDeletionScenarios")
    void testScenarios(final String deletableCaseTypes,
                       final String deletableCaseTypesSimulation,
                       final String scriptPath,
                       final List<Long> initialStateRowIds,
                       final Map<String, List<Long>> indexedData,
                       final List<Long> deletableEndStateRowIds,
                       final List<Long> simulatedEndStateRowIds,
                       final List<Long> deletableCaseRefDocuments,
                       final Map<String, List<Long>> deletedFromIndexed,
                       final Map<String, List<Long>> notDeletedFromIndexed) throws Exception {
        // GIVEN
        //setupData(deletableCaseTypes, deletableCaseTypesSimulation, scriptPath, initialStateRowIds, indexedData);

        // WHEN
        //executor.execute();

        // THEN
        //verifyDatabaseDeletion(deletableEndStateRowIds);
        //verifyDocumentDeletion(deletableCaseRefDocuments);
        //verifyElasticsearchDeletion(deletedFromIndexed, notDeletedFromIndexed);
        //verifyDatabaseDeletionSimulation(simulatedEndStateRowIds);
    }
}
