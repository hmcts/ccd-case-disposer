package uk.gov.hmcts.reform.ccd;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.helper.GlobalSearchIndexCreator;
import uk.gov.hmcts.reform.ccd.utils.CcdDatastoreTestUtils;
import uk.gov.hmcts.reform.ccd.utils.DocumentDeleteTestUtils;
import uk.gov.hmcts.reform.ccd.utils.ElasticSearchTestUtils;
import uk.gov.hmcts.reform.ccd.utils.RoleDeleteTestUtils;
import uk.gov.hmcts.reform.ccd.utils.SimulationTestUtils;

import java.util.Map;
import javax.inject.Inject;

import static uk.gov.hmcts.reform.ccd.parameter.TestParameterResolver.DELETABLE_CASE_TYPES_PROPERTY;
import static uk.gov.hmcts.reform.ccd.parameter.TestParameterResolver.DELETABLE_CASE_TYPES_PROPERTY_SIMULATION;

@Slf4j
public class TestDataProvider {

    @Inject
    private SimulationTestUtils simulationTestUtils;

    @Inject
    private ElasticSearchTestUtils elasticSearchTestUtils;

    @Inject
    private CcdDatastoreTestUtils ccdDatastoreTestUtils;

    @Inject
    private DocumentDeleteTestUtils documentDeleteTestUtils;

    @Inject
    private RoleDeleteTestUtils roleDeleteTestUtils;

    @Inject
    private GlobalSearchIndexCreator globalSearchIndexCreator;


    protected void setupData(final String deletableCaseTypes,
                             final String deletableCaseTypesSimulation,
                             final Map<String, Integer> initialStateNumberOfDatastoreRecords,
                             final Map<String, String> deletableDocuments,
                             final Map<String, String> deletableRoles) throws Exception {

        //createGlobalSearchIndex();

        clearUpSystemProperties();
        setDeletableCaseTypes(deletableCaseTypes);
        setDeletableCaseTypesSimulation(deletableCaseTypesSimulation);

        ccdDatastoreTestUtils.insertDataIntoCcdDatastore(initialStateNumberOfDatastoreRecords);
        ccdDatastoreTestUtils.verifyCcdDatastoreIsPopulated(initialStateNumberOfDatastoreRecords);

        documentDeleteTestUtils.uploadDocument(deletableDocuments);

        roleDeleteTestUtils.createRoleAssignment(deletableRoles);

        elasticSearchTestUtils.resetIndices(initialStateNumberOfDatastoreRecords.keySet());
        elasticSearchTestUtils.verifyElasticsearchRecords(initialStateNumberOfDatastoreRecords);
    }

    private void clearUpSystemProperties() {
        System.clearProperty(DELETABLE_CASE_TYPES_PROPERTY);
        System.clearProperty(DELETABLE_CASE_TYPES_PROPERTY_SIMULATION);
    }


    protected void verifyDatabaseDeletion(final Map<String, Integer> endStateNumberOfDatastoreRecords) {
        ccdDatastoreTestUtils.verifyCcdDatastoreDeletion(endStateNumberOfDatastoreRecords);
    }

    protected void verifyElasticsearchDeletion(final Map<String, Integer> endStateNumberOfDatastoreRecords) {
        elasticSearchTestUtils.verifyElasticsearchRecords(endStateNumberOfDatastoreRecords);
    }

    protected void verifyDatabaseDeletionSimulation() {
        simulationTestUtils.verifyDatabaseDeletionSimulation();
    }

    protected void verifyDocumentDeletion(final Map<String, String> deletableDocuments) {
        documentDeleteTestUtils.verifyDocumentStoreDeletion(deletableDocuments);
    }

    protected void verifyRoleDeletion(final Map<String, String> deletableRoles) {
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
