package uk.gov.hmcts.reform.ccd.data;

import uk.gov.hmcts.reform.ccd.config.WireMockStubs;
import uk.gov.hmcts.reform.ccd.config.es.TestContainers;
import uk.gov.hmcts.reform.ccd.utils.DatabaseIntegrationTestUtils;
import uk.gov.hmcts.reform.ccd.utils.DocumentDeleteIntegrationTestUtils;
import uk.gov.hmcts.reform.ccd.utils.ElasticSearchIntegrationTestUtils;
import uk.gov.hmcts.reform.ccd.utils.RoleDeleteIntegrationTestUtils;
import uk.gov.hmcts.reform.ccd.utils.SimulationIntegrationTestUtils;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import static java.lang.System.setProperty;
import static uk.gov.hmcts.reform.ccd.config.TestParameterResolver.DELETABLE_CASE_TYPES_PROPERTY;
import static uk.gov.hmcts.reform.ccd.config.TestParameterResolver.DELETABLE_CASE_TYPES_PROPERTY_SIMULATION;


public class TestDataProvider extends TestContainers {

    @Inject
    private SimulationIntegrationTestUtils simulationIntegrationTestUtils;

    @Inject
    private ElasticSearchIntegrationTestUtils elasticSearchIntegrationTestUtils;

    @Inject
    private DatabaseIntegrationTestUtils databaseIntegrationTestUtils;

    @Inject
    private DocumentDeleteIntegrationTestUtils documentDeleteIntegrationTestUtils;

    @Inject
    private RoleDeleteIntegrationTestUtils roleDeleteIntegrationTestUtils;

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

        elasticSearchIntegrationTestUtils.resetIndices(indexedData.keySet());
        elasticSearchIntegrationTestUtils.createElasticSearchIndex(indexedData);

        databaseIntegrationTestUtils.insertDataIntoDatabase(ccdScriptPath);

        databaseIntegrationTestUtils.verifyDatabaseIsPopulated(rowIds);
        elasticSearchIntegrationTestUtils.verifyCaseDataAreInElasticsearch(indexedData);
    }

    protected void verifyDatabaseDeletion(final List<Long> rowIds) {
        databaseIntegrationTestUtils.verifyDatabaseDeletion(rowIds);
    }

    protected void verifyElasticsearchDeletion(final Map<String, List<Long>> deletedFromIndexed,
                                               final Map<String, List<Long>> notDeletedFromIndexed) {
        elasticSearchIntegrationTestUtils.verifyElasticsearchDeletion(deletedFromIndexed, notDeletedFromIndexed);
    }

    protected void verifyDatabaseDeletionSimulation(final List<Long> simulatedEndStateRowIds) {
        simulationIntegrationTestUtils.verifyDatabaseDeletionSimulation(simulatedEndStateRowIds);
    }

    protected void verifyDocumentDeletion(final List<Long> documentDeletionCaseRefs) {
        documentDeleteIntegrationTestUtils.verifyDocumentStoreDeletion(documentDeletionCaseRefs);
    }

    protected void verifyRoleDeletion(final List<Long> roleDeletionCaseRefs) {
        roleDeleteIntegrationTestUtils.verifyRoleAssignmentDeletion(roleDeletionCaseRefs);
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
