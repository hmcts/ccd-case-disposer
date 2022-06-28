package uk.gov.hmcts.reform.ccd;

import uk.gov.hmcts.reform.ccd.helper.GlobalSearchIndexCreator;
import uk.gov.hmcts.reform.ccd.utils.DatabaseTestUtils;
import uk.gov.hmcts.reform.ccd.utils.DocumentDeleteTestUtils;
import uk.gov.hmcts.reform.ccd.utils.ElasticSearchTestUtils;
import uk.gov.hmcts.reform.ccd.utils.RoleDeleteTestUtil;
import uk.gov.hmcts.reform.ccd.utils.SimulationTestUtils;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import static uk.gov.hmcts.reform.ccd.parameter.TestParameterResolver.DELETABLE_CASE_TYPES_PROPERTY;
import static uk.gov.hmcts.reform.ccd.parameter.TestParameterResolver.DELETABLE_CASE_TYPES_PROPERTY_SIMULATION;

public class TestDataProvider {

    @Inject
    private SimulationTestUtils simulationTestUtils;

    @Inject
    private ElasticSearchTestUtils elasticSearchTestUtils;

    @Inject
    private DatabaseTestUtils databaseTestUtils;

    @Inject
    private DocumentDeleteTestUtils documentDeleteTestUtils;

    @Inject
    private RoleDeleteTestUtil roleDeleteTestUtils;

    @Inject
    private GlobalSearchIndexCreator globalSearchIndexCreator;


    protected void setupData(final String deletableCaseTypes,
                             final String deletableCaseTypesSimulation,
                             final String scriptPath,
                             final Map<Long, List<String>> deletableDocuments,
                             final Map<Long, List<String>> deletableRoles,
                             final List<Long> rowIds,
                             final Map<String, List<Long>> indexedData) throws Exception {
        System.clearProperty(DELETABLE_CASE_TYPES_PROPERTY);
        System.clearProperty(DELETABLE_CASE_TYPES_PROPERTY_SIMULATION);

        createGlobalSearchIndex();

        documentDeleteTestUtils.uploadDocument(deletableDocuments);
        roleDeleteTestUtils.createRoleAssignment(deletableRoles);

        elasticSearchTestUtils.resetIndices(indexedData.keySet());

        setDeletableCaseTypes(deletableCaseTypes);
        setDeletableCaseTypesSimulation(deletableCaseTypesSimulation);

        databaseTestUtils.insertDataIntoDatabase(scriptPath);

        databaseTestUtils.verifyDatabaseIsPopulated(rowIds);

        elasticSearchTestUtils.verifyCaseDataAreInElasticsearch(indexedData);
    }

    protected void verifyDatabaseDeletion(final List<Long> initialRowIds,
                                          final List<Long> endStateRowIds) {
        databaseTestUtils.verifyDatabaseDeletion(initialRowIds,endStateRowIds);
    }

    protected void verifyElasticsearchDeletion(final Map<String, List<Long>> deletedFromIndexed,
                                               final Map<String, List<Long>> notDeletedFromIndexed) {
        elasticSearchTestUtils.verifyElasticsearchDeletion(deletedFromIndexed, notDeletedFromIndexed);
    }

    protected void verifyDatabaseDeletionSimulation(final List<Long> simulatedEndStateRowIds) {
        simulationTestUtils.verifyDatabaseDeletionSimulation(simulatedEndStateRowIds);
    }

    protected void verifyDocumentDeletion(final Map<Long, List<String>> deletableDocuments) {
        documentDeleteTestUtils.verifyDocumentStoreDeletion(deletableDocuments);
    }

    protected void verifyRoleDeletion(final Map<Long, List<String>> deletableRoles) {
        roleDeleteTestUtils.verifyRoleAssignmentDeletion(deletableRoles);
    }

    private void setDeletableCaseTypes(final String value) {
        if (value != null) {
            System.setProperty(DELETABLE_CASE_TYPES_PROPERTY, value);
        }
    }

    private void setDeletableCaseTypesSimulation(final String value) {
        if (value != null) {
            System.setProperty(DELETABLE_CASE_TYPES_PROPERTY_SIMULATION, value);
        }
    }

    private void createGlobalSearchIndex() {
        globalSearchIndexCreator.createGlobalSearchIndex();
    }

}
