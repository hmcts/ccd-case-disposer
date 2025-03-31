package uk.gov.hmcts.reform.ccd;

import jakarta.inject.Inject;
import uk.gov.hmcts.reform.ccd.helper.GlobalSearchIndexCreator;
import uk.gov.hmcts.reform.ccd.util.SecurityUtil;
import uk.gov.hmcts.reform.ccd.utils.DatabaseTestUtils;
import uk.gov.hmcts.reform.ccd.utils.DocumentDeleteTestUtils;
import uk.gov.hmcts.reform.ccd.utils.ElasticSearchTestUtils;
import uk.gov.hmcts.reform.ccd.utils.HearingDocumentDeleteTestUtils;
import uk.gov.hmcts.reform.ccd.utils.LauTestUtils;
import uk.gov.hmcts.reform.ccd.utils.RoleDeleteTestUtils;
import uk.gov.hmcts.reform.ccd.utils.SimulationTestUtils;
import uk.gov.hmcts.reform.ccd.utils.TaskDeleteTestUtils;

import java.util.List;
import java.util.Map;

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
    private HearingDocumentDeleteTestUtils hearingDocumentDeleteTestUtils;

    @Inject
    private RoleDeleteTestUtils roleDeleteTestUtils;

    @Inject
    private TaskDeleteTestUtils taskDeleteTestUtils;

    @Inject
    private LauTestUtils lauTestUtils;

    @Inject
    private GlobalSearchIndexCreator globalSearchIndexCreator;

    @Inject
    private SecurityUtil securityUtil;


    protected void setupData(final String deletableCaseTypes,
                             final String deletableCaseTypesSimulation,
                             final String scriptPath,
                             final Map<Long, List<String>> deletableDocuments,
                             final Map<Long, List<String>> deletableRoles,
                             final List<Long> rowIds,
                             final Map<String, List<Long>> indexedData) throws Exception {
        System.clearProperty(DELETABLE_CASE_TYPES_PROPERTY);
        System.clearProperty(DELETABLE_CASE_TYPES_PROPERTY_SIMULATION);

        securityUtil.generateTokens();

        createGlobalSearchIndex();

        documentDeleteTestUtils.uploadDocument(deletableDocuments);
        roleDeleteTestUtils.createRoleAssignment(deletableRoles);

        elasticSearchTestUtils.resetIndices();

        setDeletableCaseTypes(deletableCaseTypes);
        setDeletableCaseTypesSimulation(deletableCaseTypesSimulation);

        databaseTestUtils.insertDataIntoDatabase(scriptPath);

        databaseTestUtils.verifyDatabaseIsPopulated(rowIds);

        elasticSearchTestUtils.verifyCaseDataAreInElasticsearch(indexedData);
    }

    protected void verifyDatabaseDeletion(final List<Long> initialRowIds,
                                          final List<Long> endStateRowIds) {
        databaseTestUtils.verifyDatabaseDeletion(initialRowIds, endStateRowIds);
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

    protected void verifyHearingDocumentDeletion(final Map<String, List<Long>> deletedFromIndexed) {
        hearingDocumentDeleteTestUtils.verifyHearingDocumentStoreDeletion(deletedFromIndexed);
    }

    protected void verifyRoleDeletion(final Map<Long, List<String>> deletableRoles) {
        roleDeleteTestUtils.verifyRoleAssignmentDeletion(deletableRoles);
    }

    protected void verifyTaskDeletion(List<Long> deletableRowIds) {
        taskDeleteTestUtils.verifyTasksDeletion(deletableRowIds);
    }

    protected void verifyLauLogs(final List<List<Long>> roleDeletionCaseRefs) {
        lauTestUtils.verifyLauLogs(roleDeletionCaseRefs);
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
