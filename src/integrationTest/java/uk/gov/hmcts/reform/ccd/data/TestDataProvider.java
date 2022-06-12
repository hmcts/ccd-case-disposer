package uk.gov.hmcts.reform.ccd.data;

import uk.gov.hmcts.reform.ccd.config.WireMockStubs;
import uk.gov.hmcts.reform.ccd.config.es.TestContainers;
import uk.gov.hmcts.reform.ccd.utils.DatabaseTestUtils;
import uk.gov.hmcts.reform.ccd.utils.DocumentDeleteTestUtils;
import uk.gov.hmcts.reform.ccd.utils.ElasticSearchTestUtils;
import uk.gov.hmcts.reform.ccd.utils.RoleDeleteTestUtils;
import uk.gov.hmcts.reform.ccd.utils.SimulationTestUtils;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import static java.lang.System.setProperty;
import static uk.gov.hmcts.reform.ccd.config.TestParameterResolver.DELETABLE_CASE_TYPES_PROPERTY;
import static uk.gov.hmcts.reform.ccd.config.TestParameterResolver.DELETABLE_CASE_TYPES_PROPERTY_SIMULATION;


public class TestDataProvider extends TestContainers {

    @Inject
    private SimulationTestUtils simulationTestUtils;

    @Inject
    private ElasticSearchTestUtils elasticSearchTestUtils;

    @Inject
    private DatabaseTestUtils databaseTestUtils;

    @Inject
    private DocumentDeleteTestUtils documentDeleteTestUtils;

    @Inject
    private RoleDeleteTestUtils roleDeleteTestUtils;

    @Inject
    private WireMockStubs wireMockStubs;

    protected void setupData(final String deletableCaseTypes,
                             final String deletableCaseTypesSimulation,
                             final String ccdScriptPath,
                             final List<Long> rowIds,
                             final Map<String, List<Long>> indexedData) throws Exception {

        setDeletableCaseTypes(deletableCaseTypes);
        setDeletableCaseTypesSimulation(deletableCaseTypesSimulation);

        wireMockStubs.setUpStubs(WIREMOCK_SERVER);

        elasticSearchTestUtils.resetIndices(indexedData.keySet());
        elasticSearchTestUtils.createElasticSearchIndex(indexedData);

        databaseTestUtils.insertDataIntoDatabase(ccdScriptPath);

        databaseTestUtils.verifyDatabaseIsPopulated(rowIds);
        elasticSearchTestUtils.verifyCaseDataAreInElasticsearch(indexedData);
    }

    protected void verifyDatabaseDeletion(final List<Long> rowIds) {
        databaseTestUtils.verifyDatabaseDeletion(rowIds);
    }

    protected void verifyElasticsearchDeletion(final Map<String, List<Long>> deletedFromIndexed,
                                               final Map<String, List<Long>> notDeletedFromIndexed) {
        elasticSearchTestUtils.verifyElasticsearchDeletion(deletedFromIndexed, notDeletedFromIndexed);
    }

    protected void verifyDatabaseDeletionSimulation(final List<Long> simulatedEndStateRowIds) {
        simulationTestUtils.verifyDatabaseDeletionSimulation(simulatedEndStateRowIds);
    }

    protected void verifyDocumentDeletion(final List<Long> documentDeletionCaseRefs) {
        documentDeleteTestUtils.verifyDocumentStoreDeletion(documentDeletionCaseRefs);
    }

    protected void verifyRoleDeletion(final List<Long> roleDeletionCaseRefs) {
        roleDeleteTestUtils.verifyRoleAssignmentDeletion(roleDeletionCaseRefs);
    }

    private void setDeletableCaseTypes(final String value) {
        if (value != null) {
            setProperty(DELETABLE_CASE_TYPES_PROPERTY, value);
        }
    }

    private void setDeletableCaseTypesSimulation(final String value) {
        if (value != null) {
            setProperty(DELETABLE_CASE_TYPES_PROPERTY_SIMULATION, value);
        }
    }
}
